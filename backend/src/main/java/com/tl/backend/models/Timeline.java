package com.tl.backend.models;

import com.tl.backend.fileHandling.FileResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "timelines")
@Data
@NoArgsConstructor
public class Timeline {

    @Id
    private String id;
    @DBRef
    private User user;
    private String description;
    private String descriptionTitle;
    private String eventId;
    private List<FileResource> pictures;
    private List<InteractionEvent> viewsDetails = new ArrayList<>();
    private long views = 0;
    private long trendingViews = 0;
    private LocalDate creationDate = LocalDate.now();
    private List<InteractionEvent> likes = new ArrayList<>();

    public void viewsNumber(){
        views += 1;
        long counter = 0;
        for (InteractionEvent view : viewsDetails){
            if (Period.between(view.getDate(), LocalDate.now()).getDays() <= 2){
                counter += 1;
            }
        }
        trendingViews = counter;
    }
}
