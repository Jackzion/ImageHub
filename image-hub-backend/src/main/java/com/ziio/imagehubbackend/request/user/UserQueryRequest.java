package com.ziio.imagehubbackend.request.user;

import com.ziio.imagehubbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String userName;

    private String userAccount;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private static final long serialVersionUID = 1L;

}
