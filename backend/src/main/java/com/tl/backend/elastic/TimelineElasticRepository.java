package com.tl.backend.elastic;

import com.tl.backend.models.Timeline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface TimelineElasticRepository extends ElasticsearchRepository<Timeline, String> {

    @Query("{\"bool\": {\"must\": [{\"match\": {\"authors.name\": \"?0\"}}]}}")
    Page<Timeline> findByDescriptionTitle(String descriptionTitle, Pageable pageable);
}
