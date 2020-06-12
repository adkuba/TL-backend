package com.tl.backend.models;

import com.tl.backend.fileHandling.FileResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "timelines")
@Data
@NoArgsConstructor
public class Timeline {

    @Id
    private String id;

    private User user;

    private String description;

    private String descriptionTitle;

    private Event event;

    private List<FileResource> pictures;

    private long views = 0;
    private long dayBeforeViews = 0;
    private long twoDaysBeforeViews = 0;
    private long trendingViews = 0;
    private LocalDate creationDate = LocalDate.now();
    private long likes = 0;

    public void updateTrending(){
        trendingViews = views - twoDaysBeforeViews;
    }
}
