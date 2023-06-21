package com.example.MeetSchedulerBot.service;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface MeetingRepository extends CrudRepository<Meeting, Long> {
    boolean existsByPassphrase(String passphrase);
    Meeting findMeetingByPassphrase(String passphrase);

    boolean existsByChatIdAndAndPassphrase(Long chatId, String passphrase);

    @Query(value = "SELECT m.month FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = true", nativeQuery = true)
    int findMonthByPassphrase(@Param("passphrase") String passphrase);

    @Query(value = "SELECT m.name FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = true", nativeQuery = true)
    String findOwnerByPassphrase(@Param("passphrase") String passphrase);

    @Query(value = "SELECT STRING_AGG(m.name, ', ') FROM Meeting m WHERE m.passphrase = :passphrase", nativeQuery = true)
    String concatenateFirstNamesByPassphrase(@Param("passphrase") String passphrase);

    @Query(value = "SELECT STRING_AGG(m.dates, '----') FROM Meeting m WHERE m.passphrase = :passphrase", nativeQuery = true)
    String concatenateDatesByPassphrase(@Param("passphrase") String passphrase);

}
