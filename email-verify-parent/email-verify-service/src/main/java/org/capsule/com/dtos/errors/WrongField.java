package org.capsule.com.dtos.errors;

import org.capsule.com.dtos.CommonDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WrongField implements CommonDTO {

    private String field;

    private String error;
}