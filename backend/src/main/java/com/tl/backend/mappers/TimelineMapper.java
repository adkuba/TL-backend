package com.tl.backend.mappers;

import com.tl.backend.models.Event;
import com.tl.backend.models.Timeline;
import com.tl.backend.response.EventResponse;
import com.tl.backend.response.FullTimelineResponse;
import com.tl.backend.response.TimelineResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {FileResourceMapper.class, UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimelineMapper {

    @BeanMapping(resultType = TimelineResponse.class)
    TimelineResponse timelineResponse(Timeline timeline);

    @IterableMapping(elementTargetType = TimelineResponse.class)
    List<TimelineResponse> timelinesResponse(List<Timeline> timelines);

    @BeanMapping(resultType = FullTimelineResponse.class)
    FullTimelineResponse fullTimelineResponse(Timeline timeline);
}
