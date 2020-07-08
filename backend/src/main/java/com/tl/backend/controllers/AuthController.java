package com.tl.backend.controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.tl.backend.config.AppProperties;
import com.tl.backend.mappers.UserMapper;
import com.tl.backend.models.*;
import com.tl.backend.repositories.RoleRepository;
import com.tl.backend.repositories.StatisticsRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.request.LoginRequest;
import com.tl.backend.request.SignupRequest;
import com.tl.backend.response.JwtResponse;
import com.tl.backend.response.MessageResponse;
import com.tl.backend.response.UserResponse;
import com.tl.backend.security.JwtUtils;
import com.tl.backend.services.CaptchaService;
import com.tl.backend.services.UserDetailsImpl;
import com.tl.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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

    @Autowired
    public AuthController(UserService userService, CaptchaService captchaService, StatisticsRepository statisticsRepository, UserMapper userMapper, AppProperties appProperties, AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils){
        this.appProperties = appProperties;
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
        cookie.setMaxAge(60*60*24*30);
        cookie.setPath("/");

        return cookie;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) throws StripeException {
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

        //refresh token
        String refreshToken = refreshUserToken(userDetails.getEmail());
        response.addCookie(createCookie("refresh_token", refreshToken, true));

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

            //refresh token
            String refreshToken = refreshUserToken(requestedUser.getEmail());
            response.addCookie(createCookie("refresh_token", refreshToken, true));

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

        user.setRoles(roles);
        userRepository.save(user);

        //main stats
        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
        if (optionalStatistics.isPresent()){
            Statistics statistics = optionalStatistics.get();
            statistics.setNumberOfUsers(statistics.getNumberOfUsers() + 1);
            statisticsRepository.save(statistics);
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
}