package com.msa.service;

import java.util.List;

import com.msa.dto.request.ReviewReqDTO;
import com.msa.dto.response.CommentDTO;
import com.msa.dto.response.ReviewDTO;
import com.msa.dto.response.ReviewInfoDTO;
import com.msa.dto.response.ReviewerDTO;

public interface ReviewService {

	public List<ReviewDTO> getReviewList(ReviewReqDTO reviewReqDTO);
	public ReviewInfoDTO getReviewListInfo(ReviewReqDTO reviewReqDTO);
	public List<ReviewDTO> getPowerReview();
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
