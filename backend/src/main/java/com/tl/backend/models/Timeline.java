package com.tl.backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "timelines")
@Data
@NoArgsConstructor
public class Timeline {

    @Id
    private String id;
    @DBRef
    private User user;
    private Boolean premium = false;
    private Boolean active = true;
    private String description;
    private String descriptionTitle;
    private String eventId;
    private List<FileResource> pictures = new ArrayList<>();
    private Map<LocalDate, Map<String, Long>> viewsDetails = new HashMap<>();
    private long views = 0;
    private long trendingViews = 0;
    private Boolean reported = false;
    private long numberOfReports = 0;
    private long premiumViews = 0;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate creationDate = LocalDate.now();
    private List<InteractionEvent> likes = new ArrayList<>();

    public void viewsNumber(){
        views += 1;
        long counter = 0;
        for (LocalDate day : viewsDetails.keySet()){
            if (Period.between(day, LocalDate.now()).getDays() <= 2){
                Map<String, Long> deviceInDay = viewsDetails.get(day);
                for (Long views : deviceInDay.values()){
                    counter += views;
                }
            }
        }
        trendingViews = counter;
    }
}
