package com.julien.search.service

import com.julien.search.dao.HistoryDAO
import com.julien.search.dao.VideoDownloadDAO
import com.julien.search.dao.SearchDAO
import com.julien.search.dao.UserDAO
import com.julien.search.model.YoutubeVideo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class YoutubeSearchService : SearchService {

    @Autowired
    private lateinit var historyDAO: HistoryDAO

    @Autowired
    private lateinit var searchDAO: SearchDAO

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var videoDownloadDAO: VideoDownloadDAO

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun search(userId: Int, query: String): List<YoutubeVideo> {

        logger.debug("search(userId=$userId, query=$query)")

        userDAO.validateUserName(userId)

        val response: List<YoutubeVideo> = searchDAO.search(query)

        logger.debug("search(userId=$userId, query=$query) RESPONSE: $response")

        return response
    }

    override fun searchAndDownload(userId: Int, query: String): YoutubeVideo? {

        logger.debug("searchAndDownload(userId=$userId, query=$query)")

        userDAO.validateUserName(userId)

        val videoList: List<YoutubeVideo> = searchDAO.search(query)

        logger.debug("searchAndDownload(userId=$userId, query=$query) SEARCH RESPONSE: $videoList")

        val result: YoutubeVideo? = downloadVideo(videoList)

        logger.debug("searchAndDownload(userId=$userId, query=$query) DOWNLOAD RESPONSE: $result")

        historyDAO.save(query, result)

        return result
    }

    private fun downloadVideo(videoList: List<YoutubeVideo>): YoutubeVideo? {
        for (video in videoList) {
            val downloadedVideo = videoDownloadDAO.download(video)
            if (downloadedVideo?.filename != null) {
                return downloadedVideo
            }
        }
        return null
    }
}
