package com.tl.backend.mappers;

import com.tl.backend.models.Event;
import com.tl.backend.response.EventResponse;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Mapper(componentModel = "spring", uses = {FileResourceMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventsMapper {

    @IterableMapping(elementTargetType = EventResponse.class)
    List<EventResponse> eventResponse(List<Event> events);
}
