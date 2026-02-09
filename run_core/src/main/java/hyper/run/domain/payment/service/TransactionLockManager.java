package hyper.run.domain.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TransactionId 기반 락 관리자
 * 동일한 transactionId에 대한 중복 처리를 방지하기 위한 락 메커니즘 제공
 */
@Slf4j
@Component
public class TransactionLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * transactionId에 대한 락 획득
     */
    public ReentrantLock getLock(String transactionId) {
        return locks.computeIfAbsent(transactionId, key -> new ReentrantLock());
    }

    /**
     * 락 해제 및 정리
     * 락이 더 이상 사용되지 않으면 맵에서 제거하여 메모리 누수 방지
     */
    public void releaseLock(String transactionId) {
        locks.computeIfPresent(transactionId, (key, lock) -> {
            if (!lock.hasQueuedThreads()) {
                log.debug("락 제거: transactionId={}", transactionId);
                return null; // 맵에서 제거
            }
            return lock; // 대기 중인 스레드가 있으면 유지
        });
    }

    /**
     * 락 상태 확인 (디버깅용)
     */
    public int getLockCount() {
        return locks.size();
    }
}
