package com.tl.backend.repositories;

import com.tl.backend.models.DeviceInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DeviceInfoRepository extends MongoRepository<DeviceInfo, String> {

    List<DeviceInfo> findByUsername(String username);

    List<DeviceInfo> findByLocation(String location);

}
