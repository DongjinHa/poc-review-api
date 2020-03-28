package com.msa.service.aggregation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.TextCriteria;

import com.msa.dto.request.ReviewReqDTO;

public class ReviewerAggregation {

	public static Aggregation getReviewListAggr(ReviewReqDTO reviewReqDTO) {
		
		List<CriteriaDefinition> criteriaList = new ArrayList<>();
		List<CriteriaDefinition> criteriaTargetList = new ArrayList<>();
		Criteria criteriaTargetForKey = new Criteria();
		
		// A:포토리뷰, B:간단리뷰 
		criteriaList.add(Criteria.where("reviewCl").is(reviewReqDTO.getReviewCl()));
		
		// 상품코드
		if(reviewReqDTO.getPrdSeq() != null && !"".equals(reviewReqDTO.getPrdSeq())) {
			criteriaList.add(Criteria.where("prdSeq").is(reviewReqDTO.getPrdSeq()));
		}

		// 연령  
        int currentYear  = Calendar.getInstance().get(Calendar.YEAR);
        String uage = reviewReqDTO.getUage();
        
        switch(uage) {
        case "all" :
        	break;
        case "10" :
        	criteriaList.add(Criteria.where("reviewer.birthDay").gte(Integer.toString(currentYear-18)+"0101"));
        	criteriaList.add(Criteria.where("reviewer.birthDay").lte(Integer.toString(currentYear-9)+"1231"));
			break;
        case "20" :
        	criteriaList.add(Criteria.where("reviewer.birthDay").gte(Integer.toString(currentYear-28)+"0101"));
        	criteriaList.add(Criteria.where("reviewer.birthDay").lte(Integer.toString(currentYear-19)+"1231"));
			break;
        case "30" :
        	criteriaList.add(Criteria.where("reviewer.birthDay").gte(Integer.toString(currentYear-38)+"0101"));
        	criteriaList.add(Criteria.where("reviewer.birthDay").lte(Integer.toString(currentYear-29)+"1231"));
			break;
        case "40" :
        	criteriaList.add(Criteria.where("reviewer.birthDay").lte(Integer.toString(currentYear-39)+"1231"));
			break;
        }
        
		// 피부타입
		if(reviewReqDTO.getSkintypecdyn() != null && reviewReqDTO.getSkintypecdyn().equals("Y")) {
			criteriaList.add(Criteria.where("reviewer.skinTypeCd").in(reviewReqDTO.getSkintypecd1(),reviewReqDTO.getSkintypecd2(),reviewReqDTO.getSkintypecd3()
					,reviewReqDTO.getSkintypecd4(),reviewReqDTO.getSkintypecd5(),reviewReqDTO.getSkintypecd6(),reviewReqDTO.getSkintypecd7()));
		}
		
		// 피부밝기
		if(reviewReqDTO.getSkinetcinfoyn() != null && reviewReqDTO.getSkinetcinfoyn().equals("Y")) {
			criteriaList.add(Criteria.where("reviewer.skinEtcInfo").in(reviewReqDTO.getSkinetcinfo1(),reviewReqDTO.getSkinetcinfo2(),reviewReqDTO.getSkinetcinfo3()));
		}
		
		// 피부톤
		if(reviewReqDTO.getSkintonecdyn() != null && reviewReqDTO.getSkintonecdyn().equals("Y")) {
			criteriaList.add(Criteria.where("reviewer.skinToneCd").in(reviewReqDTO.getSkintonecd1(),reviewReqDTO.getSkintonecd2(),reviewReqDTO.getSkintonecd3()));
		}
		
		// 상품코드 검색(키워드로 상품정보를 조회해온 경우)
		if(reviewReqDTO.getPrdSeqList() != null && reviewReqDTO.getPrdSeqList().size() > 0) {
			//키워드 like 검색사용시 or조건적용을 위해 주석해제
			//criteriaTargetList.add(Criteria.where("prdSeq").in(reviewReqDTO.getPrdSeqList()));
		}
		
		// 키워드 검색
		MatchOperation matchByFTS = null;
		if(Strings.isEmpty(reviewReqDTO.getKey())==false) {
			//like 검색
			//criteriaTargetList.add(Criteria.where("goodCnts").regex(reviewReqDTO.getKey()));
			
			//full text search 검색
			//db.reviews.createIndex({"goodCnts":"text"}) -- 인덱스 생성필요
			TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matching(reviewReqDTO.getKey());
			matchByFTS = Aggregation.match(textCriteria);
		}

		// 키워드 검색대상이 있으면 수행
        if(criteriaTargetList.size() > 0) {
        	criteriaList.add(criteriaTargetForKey.orOperator(criteriaTargetList.toArray(new Criteria[criteriaTargetList.size()])));	
        }
        
		Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
		
		MatchOperation match = Aggregation.match(criteria);
		LookupOperation lookUp = LookupOperation.newLookup()
				.from("reviewers").localField("reviewer_id")   	//1. 묶을 컬렉션 이름은 reviewers, 대상 도큐먼트는 같은 이름인 reviewer_id
				.foreignField("_id").as("reviewer");  			//2. 조회할 컬렉션에서 해당 reviews 컬렉션이 묶일 도큐먼트 이름은 _id, 별명은 reviewer

		Aggregation aggregation;
		// 총건수 조회여부에 따라수행
		if(reviewReqDTO.getInfoYn() != null && "Y".equals(reviewReqDTO.getInfoYn())){
			GroupOperation group = Aggregation.group().avg("evalScore").as("avgScore").count().as("totCnt");
			ProjectionOperation project = Aggregation.project("totCnt").and("avgScore").substring(0, 3).as("avgScore");

			//CountOperation count = Aggregation.count().as("totCnt");

			if(matchByFTS == null)
				aggregation = Aggregation.newAggregation(lookUp, match, group, project);
			else
				aggregation = Aggregation.newAggregation(matchByFTS, lookUp, match, group, project);

		}else{
			
			// 1:최신순, 2:조회순
			SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "regDate");
			if(reviewReqDTO.getSort()==2) {
				sort = Aggregation.sort(Sort.Direction.DESC, "hit");
			}
			SkipOperation skip = Aggregation.skip((long)(reviewReqDTO.getPageNo()-1)*20);
			LimitOperation limit = Aggregation.limit(20);
			
			if(matchByFTS == null)
				aggregation = Aggregation.newAggregation(lookUp, match, sort, skip, limit);
			else
				aggregation = Aggregation.newAggregation(matchByFTS, lookUp, match, sort, skip, limit);
			
		}
		
		return aggregation;
		
	}
}
