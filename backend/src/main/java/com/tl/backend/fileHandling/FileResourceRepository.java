package com.tl.backend.fileHandling;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileResourceRepository extends MongoRepository<FileResource,String> {
}
