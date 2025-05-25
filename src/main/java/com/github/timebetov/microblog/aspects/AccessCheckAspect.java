package com.github.timebetov.microblog.aspects;

import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.utils.SecurityUtils;
import com.github.timebetov.microblog.validations.OnlyOwnerOrAdmin;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AccessCheckAspect {

    @Before("@annotation(onlyOwnerOrAdmin)")
    public void checkAccess(JoinPoint joinPoint, OnlyOwnerOrAdmin onlyOwnerOrAdmin) {

        CurrentUserContext currentUser = SecurityUtils.getCurrentUserContext();

        if (currentUser == null || currentUser.getUserId() == null) {
            throw new IllegalArgumentException("User is not logged in");
        }

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(onlyOwnerOrAdmin.ownerIdParam())) {
                Object arg = args[i];
                if (arg instanceof Long ownerId) {
                    if (!ownerId.equals(currentUser.getUserId()) && !currentUser.isAdmin()) {
                        throw new AccessDeniedException("You don't have permission to access this resource");
                    }
                    return;
                } else {
                    throw new IllegalArgumentException("Parameter '" + onlyOwnerOrAdmin.ownerIdParam() + "' must be of type Long");
                }
            }
        }

        throw new IllegalStateException("Owner ID parameter not found in method signature");
    }
}
