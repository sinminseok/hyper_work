package hyper.run.aop;

import hyper.run.annotation.DistributionLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributionLockAop {

    private static final String REDISSON_KEY_PREFIX = "lock::";
    private final RedissonClient redissonClient;
    private final AopTransaction aopTransaction;

    @Around("@annotation(hyper.run.annotation.DistributionLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributionLock annotation = method.getAnnotation(DistributionLock.class);

        String dynamicValue = String.valueOf(
            CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                annotation.key()
            )
        );
        String key = REDISSON_KEY_PREFIX + annotation.prefix() + dynamicValue;
        RLock lock = redissonClient.getLock(key);

        try {
            boolean available = lock.tryLock(annotation.waitTime(), annotation.leaseTime(), annotation.timeUnit());

            if (!available) {
                throw new IllegalStateException("Could not acquire distribution lock. key : " + key);
            }

            if (annotation.useTransaction()) {
                return aopTransaction.proceed(joinPoint);
            } else {
                return joinPoint.proceed();
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("InterruptedException occurred. key : " + key, e);
        } finally {
            // 현재 스레드가 락을 보유하고 있으면 해제
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
