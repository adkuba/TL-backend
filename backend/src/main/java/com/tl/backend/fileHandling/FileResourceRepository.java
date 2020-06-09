package com.tl.backend.fileHandling;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileResourceRepository extends MongoRepository<FileResource,String> {

    @Query("{ 'toDelete' : ?0 }")
    List<FileResource> findAllByToDelete(@Param("toDelete") Boolean toDelete);
}
