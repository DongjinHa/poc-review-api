package com.msa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.dto.CommentDTO;
import com.msa.dto.ReviewDTO;
import com.msa.dto.ReviewerDTO;
import com.msa.service.ReviewService;
 
@RestController
@RequestMapping("/review")
public class ReviewController {
	
	@Autowired
	ReviewService reviewService;

    @PostMapping("/allreview")
    public List<ReviewDTO> getReviewList(@RequestBody ReviewDTO reviewdto) {
		return reviewService.getReviewList(reviewdto);
    }
    
    @PostMapping("/allreview-info")
    public Map<String,String> getReviewListInfo(@RequestBody ReviewDTO reviewdto) {
		
		String totCnt = "0";
    	String avgScore = "0";
		reviewdto.setInfoYn("Y");
		List<ReviewDTO> list = reviewService.getReviewList(reviewdto);
		if (list != null) {
			totCnt = list.get(0).getTotCnt();
			avgScore = list.get(0).getAvgScore();
		}

		Map<String,String> map = new HashMap<>();
		map.put("TotCnt", totCnt);
		map.put("AvgScore", avgScore);

		return map; 
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
