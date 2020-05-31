package com.tl.backend.mappers;

import com.tl.backend.models.Event;
import com.tl.backend.models.Timeline;
import com.tl.backend.response.EventResponse;
import com.tl.backend.response.TimelineResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {FileResourceMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimelineMapper {

    @BeanMapping(resultType = TimelineResponse.class)
    TimelineResponse timelineResponse(Timeline timeline);
}
