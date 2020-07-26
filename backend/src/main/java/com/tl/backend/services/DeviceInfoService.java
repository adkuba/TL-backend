package com.tl.backend.services;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.models.DeviceInfo;
import com.tl.backend.response.StatResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DeviceInfoService {

    DeviceInfo createInfo(HttpServletRequest request, String username) throws IOException, GeoIp2Exception;

    List<DeviceInfo> getAll();

    List<StatResponse> getLocations(Map<LocalDate, Map<String, Long>> views);

    List<StatResponse> getViews(Map<LocalDate, Map<String, Long>> views);
}
