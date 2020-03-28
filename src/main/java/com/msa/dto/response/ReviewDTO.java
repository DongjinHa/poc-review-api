package com.msa.dto.response;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ReviewDTO {
	private String _id;	//ObjectId로 설정할 경우 DB상의 _id값과 차이 발생하여 변경
	private String reviewer_id;
	private String reviewCl;
	private String prdSeq;
	private String bestFl;
	private String evalScore;
	private int hit;
	private String recomCnt;
	private String recbScore;
	private String goodCnts;
	private String etcCnts;
	private String[] tplList;
	private Date regDate;
	private Date updDate;
	
	/*
	 Lookup시 리턴타입으로 List로 하지 않을 경우 type error발생
	 	Failed to convert from type [java.util.ArrayList<?>] to type [com.msa.dto.ReviewerDTO] 
	 */
	private List<ReviewerDTO> reviewer;	
}
