package com.starbucks.inventory.aop.cafe;

import com.starbucks.inventory.security.SecurityUtils;
import com.starbucks.inventory.repository.UserRepository;
import com.starbucks.inventory.domain.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Filter;
import java.util.Optional;

@Aspect
@Component
public class CafeAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private final String fieldName =  "cafeId";

    private final Logger log = LoggerFactory.getLogger(CafeAspect.class);

    /**
     * Run method if User service is hit.
     * Filter users based on which cafe the user is associated with.
     * Skip filter if user has no cafe
     */
    @Before("execution(* com.starbucks.inventory.service.UserService.*(..)) || execution(* com.starbucks.inventory.service.AssetService.*(..))")
    public void beforeExecution() throws Throwable {
        Optional<String> login = SecurityUtils.getCurrentUserLogin();

        if(login.isPresent()) {
			User user = userRepository.findOneByLogin(login.get()).get();

			if (user.getCafe() != null) {
				Filter filter = entityManager.unwrap(Session.class).enableFilter("CAFE_FILTER");
				filter.setParameter(fieldName, user.getCafe().getId());
			}
		}
    }
}
