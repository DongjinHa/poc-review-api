package com.msa.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class ReviewReqDTO {
    
    private int pageNo;
	private String reviewCl;
	private int sort;
	private String prdSeq;
	private String key;
	private String uage = "all";
	private String skintypecd1;
	private String skintypecd2;
	private String skintypecd3;
	private String skintypecd4;
	private String skintypecd5;
	private String skintypecd6;
	private String skintypecd7;
	private String skintypecdyn;
	private String skinetcinfo1;
	private String skinetcinfo2;
	private String skinetcinfo3;
	private String skinetcinfoyn;
	private String skintonecd1;
	private String skintonecd2;
	private String skintonecd3;
	private String skintonecdyn;
	private List<String> prdSeqList;
	private String infoYn;

}
