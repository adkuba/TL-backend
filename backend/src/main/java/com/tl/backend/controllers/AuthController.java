package com.tl.backend.controllers;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.tl.backend.config.AppProperties;
import com.tl.backend.mappers.UserMapper;
import com.tl.backend.models.*;
import com.tl.backend.repositories.RoleRepository;
import com.tl.backend.repositories.StatisticsRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.request.LoginRequest;
import com.tl.backend.request.PasswordResetRequest;
import com.tl.backend.request.SignupRequest;
import com.tl.backend.response.JwtResponse;
import com.tl.backend.response.MessageResponse;
import com.tl.backend.response.UserResponse;
import com.tl.backend.security.JwtUtils;
import com.tl.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.WebUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppProperties appProperties;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final CaptchaService captchaService;
    private final StatisticsRepository statisticsRepository;
    private final JavaMailSender emailSender;
    private final DeviceInfoServiceImpl deviceInfoService;
    private final StatisticsServiceImpl statisticsService;
    private final NotificationServiceImpl notificationService;

    @Autowired
    public AuthController(NotificationServiceImpl notificationService, StatisticsServiceImpl statisticsService, DeviceInfoServiceImpl deviceInfoService, JavaMailSender emailSender, UserService userService, CaptchaService captchaService, StatisticsRepository statisticsRepository, UserMapper userMapper, AppProperties appProperties, AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils){
        this.appProperties = appProperties;
        this.notificationService = notificationService;
        this.statisticsService = statisticsService;
        this.deviceInfoService = deviceInfoService;
        this.emailSender = emailSender;
        this.userService = userService;
        this.captchaService = captchaService;
        this.statisticsRepository = statisticsRepository;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    private String refreshUserToken(String email){
        User requestedUser = userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"user does not exist")
        );
        UUID refresh_token = UUID.randomUUID();
        requestedUser.setRefreshToken(refresh_token.toString());
        userRepository.save(requestedUser);

        return refresh_token.toString();
    }

    private Cookie createCookie(String name, String value, Boolean httpOnly){
        Cookie cookie = null;
        cookie = new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8));
        cookie.setDomain(appProperties.getDomain());
        cookie.setHttpOnly(httpOnly);

        //for production https
        cookie.setSecure(true);

        cookie.setMaxAge(60*60*24*30);
        cookie.setPath("/");

        return cookie;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(HttpServletRequest request, @Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) throws StripeException, IOException, GeoIp2Exception {
        //logowanie
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Date creationTime = new Date();
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        //update subscription info every login
        userService.checkSubscription(loginRequest.getUsername());
        Optional<User> optionalUser = userRepository.findUserByEmail(userDetails.getEmail());
        UserResponse userResponse = new UserResponse();
        if (optionalUser.isPresent()){
            userResponse = userMapper.userResponse(optionalUser.get());
        }
        if (userResponse.getBlocked()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is blocked!");
        }

        //refresh token
        String refreshToken = refreshUserToken(userDetails.getEmail());
        response.addCookie(createCookie("refresh_token", refreshToken, true));

        deviceInfoService.createInfo(request, userResponse.getUsername());

        return ResponseEntity.ok(new JwtResponse(jwt,
                creationTime,
                userResponse));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws StripeException {
        Cookie refreshTokenCookie = WebUtils.getCookie(request,"refresh_token");

        if (refreshTokenCookie != null) {
            User requestedUser = userRepository.findUserByRefreshToken(refreshTokenCookie.getValue()).
                    orElseThrow( () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid refresh token!"));

            //tylko po username
            Date creationTime = new Date();
            String jwt = jwtUtils.refreshJwtToken(requestedUser.getUsername());

            //update subscription info every login
            userService.checkSubscription(requestedUser.getUsername());
            Optional<User> optionalUser = userRepository.findUserByEmail(requestedUser.getEmail());
            UserResponse userResponse = new UserResponse();
            if (optionalUser.isPresent()){
                userResponse = userMapper.userResponse(optionalUser.get());
            }
            if (userResponse.getBlocked()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is blocked!");
            }

            //refresh token
            String refreshToken = refreshUserToken(requestedUser.getEmail());
            response.addCookie(createCookie("refresh_token", refreshToken, true));

            deviceInfoService.createInfo(request, userResponse.getUsername());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    creationTime,
                    userResponse));

        }else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid refresh token!");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) throws StripeException {
        if (signUpRequest.getRecaptchaToken() != null){
            if (!captchaService.processResponse(signUpRequest.getRecaptchaToken())){
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Bad captcha verification"));
            }
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                if ("admin".equals(role)) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(userRole);
                }
            });
        }

        //STRIPE
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("email", signUpRequest.getEmail());
        Customer customer = Customer.create(customerParams);
        user.setStripeID(customer.getId());

        user.setFullName(signUpRequest.getFullName());

        user.setRoles(roles);
        userRepository.save(user);
        notificationService.createNotification(user.getUsername());

        //main stats
        statisticsService.checkStatistics();
        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
        if (optionalStatistics.isPresent()){
            Statistics statistics = optionalStatistics.get();
            statistics.setNumberOfUsers(statistics.getNumberOfUsers() + 1);
            statisticsRepository.save(statistics);
        }

        //email
        try {
            MimeMessage message = emailSender.createMimeMessage();
            message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(signUpRequest.getEmail()));
            message.setSubject("Welcome");
            message.setContent(appProperties.getMailBeginning() + "Welcome " + appProperties.getMailMid() + "Thank you for creating account!\n\n User: " + user.getUsername() + " start discover new timelines on our homepage. \n\n That's not you? Take control of this account and reset password!" + appProperties.getMailEnd() , "text/html");
            emailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            //e.printStackTrace();
            ResponseEntity.badRequest().body(new MessageResponse("Can't send email, you can still login."));
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        Cookie refreshTokenCookie = WebUtils.getCookie(request,"refresh_token");
        if (refreshTokenCookie != null){

            Optional<User> requestedUser = userRepository.findUserByRefreshToken(refreshTokenCookie.getValue());

            if(requestedUser.isPresent()) {
                User u = requestedUser.get();
                u.setRefreshToken("");
                userRepository.save(u);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestParam String email){
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(token);
            user.setPasswordResetToken(passwordResetToken);
            userRepository.save(user);

            try {
                MimeMessage message = emailSender.createMimeMessage();
                message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject("Reset password");
                message.setContent(appProperties.getMailBeginning() + "Reset your password " + appProperties.getMailMid() + "Click this link <a href='http://localhost:8080/passwordReset/" + token + "'>reset</a> \n\n That's not you? Change password! " + appProperties.getMailEnd(), "text/html");
                emailSender.send(message);
                return ResponseEntity.ok(new MessageResponse("Email send!"));

            } catch (MessagingException | UnsupportedEncodingException e) {
                //e.printStackTrace();
                return ResponseEntity.badRequest().body(new MessageResponse("Can't send email"));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Can't find user"));
    }

    @PostMapping("/resetPasswordChange")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest){
        Optional<User> optionalUser = userRepository.findByPasswordResetToken(passwordResetRequest.getToken());
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            if (user.getPasswordResetToken().getExpiryDate().compareTo(LocalDate.now()) >= 0){
                user.setPassword(encoder.encode(passwordResetRequest.getNewPassword()));
                user.setPasswordResetToken(null);
                user.setRefreshToken(null);
                userRepository.save(user);
                try {
                    MimeMessage message = emailSender.createMimeMessage();
                    message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
                    message.setSubject("Password changed");
                    message.setContent(appProperties.getMailBeginning() + "New password " + appProperties.getMailMid() + "Your password has been changed." + "\n\n You didn't changed your password? Reset it! " + appProperties.getMailEnd(), "text/html");
                    emailSender.send(message);
                } catch (MessagingException | UnsupportedEncodingException e) {
                    //e.printStackTrace();
                }
                return ResponseEntity.ok(new MessageResponse("Password changed!"));

            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Token expired! Send email again."));
            }

        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Bad token! Send email again."));
        }
    }
}