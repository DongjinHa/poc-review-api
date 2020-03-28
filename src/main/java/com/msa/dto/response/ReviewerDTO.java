package com.msa.dto.response;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class ReviewerDTO {
	@Id
	private String _id;
	private String nickNm;
	private String lvl;
	private String sex;
	private String birthDay;
	private String skinToneCd;
	private String skinTypeCd;
	private String[] skinTrublList;
	private String skinEtcInfo;
	private String profileImg;
	private String regDate;
	private String updDate;
}
