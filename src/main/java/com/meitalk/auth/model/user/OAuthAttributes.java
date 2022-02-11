package com.meitalk.auth.model.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String registraionId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture, String registraionId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.registraionId = registraionId;
    }

    public static OAuthAttributes ofG(String registraionId, String userNameAttributeName, Map<String, Object> attributes) {
        return ofGoogle(registraionId, userNameAttributeName, attributes);
    }

    public static OAuthAttributes ofF(String registraionId, String userNameAttributeName, Map<String, Object> attributes) {
        return ofFacebook(registraionId, userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String registraionId,
                                            String userNameAttributeName,
                                            Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .registraionId(registraionId)
                .build();
    }

    private static OAuthAttributes ofFacebook(String registraionId,
                                              String userNameAttributeName,
                                              Map<String, Object> attributes) {
        ObjectMapper mapper = new ObjectMapper();
        FacebookPictureDataDto facebookPictureDto = mapper.convertValue(attributes.get("picture"), FacebookPictureDataDto.class);
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email") == null ? (String) attributes.get("id") + "@facebook.com" : (String) attributes.get("email"))
                .picture(facebookPictureDto.getData().getUrl())
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .registraionId(registraionId)
                .build();
    }
}
