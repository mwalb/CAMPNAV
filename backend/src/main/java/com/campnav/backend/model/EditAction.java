package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Map;

@Data
@Entity
public class EditAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EditOperation operation;
    
    @ElementCollection
    @CollectionTable(name = "edit_action_params", joinColumns = @JoinColumn(name = "edit_action_id"))
    @MapKeyColumn(name = "param_key")
    @Column(name = "param_value")
    private Map<String, String> params;
}
