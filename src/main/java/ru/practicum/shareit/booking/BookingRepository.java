package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId ORDER BY b.start DESC")
    List<Booking> findByBookerIdOrderByStartDesc(@Param("bookerId") Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC")
    List<Booking> findLastBooking(@Param("itemId") Long itemId,
                                  @Param("now") LocalDateTime now,
                                  Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBooking(@Param("itemId") Long itemId,
                                  @Param("now") LocalDateTime now,
                                  Pageable pageable);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.item.id = :itemId " +
            "AND b.status = :status " +
            "AND b.end < :now")
    boolean existsApprovedBooking(@Param("userId") Long userId,
                                  @Param("itemId") Long itemId,
                                  @Param("status") BookingStatus status,
                                  @Param("now") LocalDateTime now);
}