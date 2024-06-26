package auth.service.dev.dtos.responses.tokens;

import auth.service.dev.dtos.CommonDTO;
import auth.service.dev.dtos.responses.entities.Credentials;
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
public class TokensPayloadResp extends CommonDTO {

    private Token access;

    private Token refresh;

    private Credentials data;
}