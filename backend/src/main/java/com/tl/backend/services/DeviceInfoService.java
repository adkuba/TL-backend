package com.tl.backend.services;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.models.DeviceInfo;
import com.tl.backend.response.StatResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface DeviceInfoService {

    DeviceInfo createInfo(HttpServletRequest request, String username) throws IOException, GeoIp2Exception;

    List<DeviceInfo> getAll();

    List<StatResponse> getLocations(String timelineId);

    List<StatResponse> getViews(String timelineId);
}
