package com.meitalk.auth.mapper;


import com.meitalk.auth.model.user.CustomUserDetails;
import com.meitalk.auth.model.user.ResEmailVerification;
import com.meitalk.auth.model.user.UserJoinDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {

    CustomUserDetails selectUserByEmail(String mailId);

    int insertUserJoin(UserJoinDto req);

    CustomUserDetails selectUserByOauth2Key(String oauth2Key);

    ResEmailVerification selectEmailVerificationByUserNoByFlag(String userNo, String flag);

    void updateGoogleOauth2Key(String userEmail, String sub);

    void updateFacebookOauth2Key(String userEmail, String id);

    void updateAppleOauth2Key(String userEmail, String sub);

    int updateRole(
            @Param("userMail") String userMail,
            @Param("role") String role
    );
}
