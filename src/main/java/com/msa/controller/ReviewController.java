package com.msa.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.dto.request.ReviewReqDTO;
import com.msa.dto.response.CommentDTO;
import com.msa.dto.response.ReviewDTO;
import com.msa.dto.response.ReviewInfoDTO;
import com.msa.dto.response.ReviewerDTO;
import com.msa.service.ReviewService;
 
@RestController
@RequestMapping("/review")
public class ReviewController {
	
	@Autowired
	ReviewService reviewService;

    @PostMapping("/allreview")
    public List<ReviewDTO> getReviewList(@RequestBody ReviewReqDTO reviewReqDTO) {
		return reviewService.getReviewList(reviewReqDTO);
    }
    
    @PostMapping("/allreview-info")
    public ReviewInfoDTO getReviewListInfo(@RequestBody ReviewReqDTO reviewReqDTO) {	
    	reviewReqDTO.setInfoYn("Y");
    	ReviewInfoDTO data = reviewService.getReviewListInfo(reviewReqDTO);

		return data==null? new ReviewInfoDTO():data;
    }

    @GetMapping("/powerreview")
    public List<ReviewDTO> getPowerReview() {
        return reviewService.getPowerReview();
    }   
    
    @GetMapping("/getReview/{id}")
    public ReviewDTO getReview(@PathVariable String id) {	
        return reviewService.getReview(id);
    }

    @DeleteMapping("/delReview/{id}")
    public String delReview(@PathVariable String id) {
    	reviewService.delReview(id);
        return "deleted id:" + id;
    }

    @GetMapping("/Reviewer/{id}")
    public ReviewerDTO getReviewer(@PathVariable String id) {
    	return reviewService.getReviewer(id);
    	
    }
    
    @GetMapping("/Comments/{id}")
    public List<CommentDTO> getComments(@PathVariable String id) {
    	return reviewService.getComments2(id);
    	
    }

    @GetMapping("/Comments/{id}/{pageNo}")
    public List<CommentDTO> getMoreComments(@PathVariable String id, @PathVariable int pageNo) {
    	return reviewService.getMoreComments(id, pageNo);
    	
    }
    
    @GetMapping("/CommentsCount/{id}")
    public int getCommentsTotalCount(@PathVariable String id) {
    	return reviewService.getCommentsTotalCount(id);
    }
    
    @GetMapping("/createdata")
    public String createData() {
    	reviewService.createData();
    	
    	return "completed";
    }
}
