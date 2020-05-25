package com.tl.backend.models;

import com.tl.backend.fileHandling.FileResourceMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {FileResourceMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventsMapper {

    @IterableMapping(elementTargetType = EventResponse.class)
    List<EventResponse> eventResponse(List<Event> events);
}
