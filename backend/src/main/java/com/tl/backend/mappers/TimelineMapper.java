package com.tl.backend.mappers;

import com.tl.backend.models.Timeline;
import com.tl.backend.response.TimelineResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {FileResourceMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimelineMapper {

    @BeanMapping(resultType = TimelineResponse.class)
    TimelineResponse timelineResponse(Timeline timeline);
}
