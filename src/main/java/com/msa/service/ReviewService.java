package com.msa.service;

import java.util.List;

import com.msa.dto.CommentDTO;
import com.msa.dto.ReviewDTO;
import com.msa.dto.ReviewerDTO;

public interface ReviewService {

	public List<ReviewDTO> getReviewList(ReviewDTO reviewDTO);
	public List<ReviewDTO> getReviewList1();
    //public Optional<ReviewDTO> getReview(String id);
	public ReviewDTO getReview(String id);
    public void delReview(String id);
	public ReviewerDTO getReviewer(String id);

	//public List<CommentDTO> getComments(String id);
	public List<CommentDTO> getComments2(String id);
	public List<CommentDTO> getMoreComments(String id, int PageNo);
	public int getCommentsTotalCount(String id);

	public void createData();
}
