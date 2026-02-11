package hyper.run.domain.banner.repository;

import hyper.run.domain.banner.entity.Banner;
import hyper.run.domain.banner.entity.BannerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    @Query("SELECT b FROM Banner b ORDER BY b.displayOrder ASC")
    Page<Banner> findAllOrderByDisplayOrder(Pageable pageable);

    @Query("SELECT b FROM Banner b WHERE b.status = :status ORDER BY b.displayOrder ASC")
    Page<Banner> findByStatusOrderByDisplayOrder(@Param("status") BannerStatus status, Pageable pageable);
}
