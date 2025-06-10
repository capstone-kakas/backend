package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.dto.CrawlingResultDto;
import com.capstone.kakas.crawlingdb.dto.FilteredResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TitleFilteringService {

    public List<CrawlingResultDto> filteringTitle(List<CrawlingResultDto> crawlingResult){

        return null;
    }
}
