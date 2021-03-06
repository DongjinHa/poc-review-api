package com.msa.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.util.Strings;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;

import com.msa.document.Comment;
import com.msa.document.Review;
import com.msa.document.Reviewer;
import com.msa.dto.request.ReviewReqDTO;
import com.msa.dto.response.CommentDTO;
import com.msa.dto.response.ReviewDTO;
import com.msa.dto.response.ReviewInfoDTO;
import com.msa.dto.response.ReviewerDTO;
import com.msa.repository.CommentRepository;
import com.msa.repository.ReviewRepository;
import com.msa.repository.ReviewerRepository;
import com.msa.service.aggregation.ReviewerAggregation;

@Service
public class ReviewServiceImpl implements ReviewService {

	@Autowired
	ReviewerRepository reviewerRepository;

	@Autowired
	ReviewRepository reviewRepository;
	
	@Autowired
	CommentRepository commentRepository;
	
	private final MongoTemplate mongoTemplate;
	
	public ReviewServiceImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	public List<ReviewDTO> getReviewList(ReviewReqDTO reviewReqDTO) {
		
		Aggregation aggregation = ReviewerAggregation.getReviewListAggr(reviewReqDTO);
	    AggregationResults<ReviewDTO> result = mongoTemplate.aggregate(aggregation, Review.class, ReviewDTO.class);
	    
		return result.getMappedResults(); 
	}
	
	public ReviewInfoDTO getReviewListInfo(ReviewReqDTO reviewReqDTO) {
		
		Aggregation aggregation = ReviewerAggregation.getReviewListAggr(reviewReqDTO);
	    AggregationResults<ReviewInfoDTO> result = mongoTemplate.aggregate(aggregation, Review.class, ReviewInfoDTO.class);
	    
		return result.getUniqueMappedResult();
	}
	
	public List<ReviewDTO> getPowerReview() {		//파워리뷰 출력을 위한 서비스
		/*Query query = new Query()
				.addCriteria(Criteria.where("bestFl").is("Y"))
				.addCriteria(Criteria.where("reviewCl").is("A"))
				.with(Sort.by(Sort.Order.desc("hit")))
				.limit(15);
		return mongoTemplate.find(query, ReviewDTO.class);    */
		
		//lookup 사용ver.
		Criteria criteria = new Criteria();
		criteria.andOperator(
				Criteria.where("bestFl").is("Y"),
				Criteria.where("reviewCl").is("A")	//포토리뷰만 출력하도록 임시조치
		);
		
		MatchOperation match = Aggregation.match(criteria);
		SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "regDate");
		LimitOperation limit = Aggregation.limit(15);
		
		LookupOperation lookUp = LookupOperation.newLookup()
				.from("reviewers").localField("reviewer_id")
				.foreignField("_id").as("reviewer"); 
		
		
		Aggregation aggregation = Aggregation.newAggregation(match, lookUp, sort, limit);
		AggregationResults<ReviewDTO> result = mongoTemplate.aggregate(aggregation, Review.class, ReviewDTO.class);
	    
		return result.getMappedResults();  
	} 
	
    public ReviewDTO getReview(String id) {
    	ReviewDTO review = mongoTemplate.findById(new ObjectId(id), ReviewDTO.class,"reviews");
    	return review;
    }
    
    public void delReview(String id) {
    	reviewRepository.deleteById(id);
    }
    
    public ReviewerDTO getReviewer(String id) {
    	ReviewerDTO reviewer = mongoTemplate.findById(new ObjectId(id), ReviewerDTO.class,"reviewers");
    	return reviewer;
    }
    
    //댓글 가져오기 (old)
//	public List<CommentDTO> getComments(String id) {
//		Query query = new Query()
//				.addCriteria(Criteria.where("review_id").is(id))	//조건 추가
//				.with(Sort.by(Sort.Order.asc("regDate")))
//				.limit(3);
//		List<CommentDTO> comments =  mongoTemplate.find(query, CommentDTO.class);    
//		
//		//코멘트를 단 유저의 정보 출력을 위하여 로직 추가
//		for(CommentDTO comment : comments) {
//			ReviewerDTO commenter = mongoTemplate.findById(new ObjectId(comment.getReviewer_id()), ReviewerDTO.class,"reviewers");
//			comment.setReviewer(commenter);
//		}
//		System.out.print(comments);
//		
//		return comments;
//	}
	
	//댓글 가져오기 (new, ObjectId)
	public List<CommentDTO> getComments2(String id) {

		LookupOperation lookUp = LookupOperation.newLookup()
				.from("reviewers").localField("reviewer_id")  
				.foreignField("_id").as("reviewer");	

		//ProjectionOperation project = Aggregation.project().andExclude("review_id");
		SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "regDate");
		LimitOperation limit = Aggregation.limit(3);
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(
																		Criteria.where("review_id").is(new ObjectId(id))
																		), lookUp, sort, limit);
	    AggregationResults<CommentDTO> result = mongoTemplate.aggregate(aggregation, Comment.class, CommentDTO.class);
	    
	    List<CommentDTO> comments = result.getMappedResults(); 
		return comments;
	}
	
	//댓글 페이징 (new)
	public List<CommentDTO> getMoreComments(String id, int pageNo) {
		
		LookupOperation lookUp = LookupOperation.newLookup()
				.from("reviewers").localField("reviewer_id")  
				.foreignField("_id").as("reviewer");  	

		SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "regDate");
		SkipOperation skip = Aggregation.skip((long)(pageNo - 1) * 3);
		LimitOperation limit = Aggregation.limit(3);
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(
																	Criteria.where("review_id").is(new ObjectId(id))
																	),lookUp, sort, skip, limit);
	    AggregationResults<CommentDTO> result = mongoTemplate.aggregate(aggregation, Comment.class, CommentDTO.class);
	    
	    List<CommentDTO> comments = result.getMappedResults();
	    
		return comments;
	}
	
	/* 댓글 페이징 (old)
	public List<CommentDTO> getMoreComments2(String id, int pageNo) {
		Query query = new Query()
				.addCriteria(Criteria.where("review_id").is(id)) // 해당하는 리뷰의 글을
				.with(Sort.by(Sort.Order.asc("regDate"))) // 등록 오름차순으로
				.skip((pageNo-1)*3) // 페이지-1개 * 3건씩 건너 뛰고 
				.limit(3); // 3개만 조회
		List<CommentDTO> comments =  mongoTemplate.find(query, CommentDTO.class);    
		
		for(CommentDTO comment : comments) {
			ReviewerDTO commenter = mongoTemplate.findById(new ObjectId(comment.getReviewer_id()), ReviewerDTO.class,"reviewers");
			comment.setReviewer(commenter);
		}
		
		return comments;
	} */
	
	// 댓글 더보기 버튼 제어를 위한 댓글 전체 수 구하기 
	public int getCommentsTotalCount(String id) {
		Query query = new Query()
				.addCriteria(Criteria.where("review_id").is(new ObjectId(id)));
		int count = mongoTemplate.find(query, CommentDTO.class).size();
		return count;
	}
	
	public void createData() {
		reviewerRepository.deleteAll();
		reviewRepository.deleteAll();
		commentRepository.deleteAll();

//		String pattern = "yyyyMMddHHmmss";
		String pattern = "yyyyMMdd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		
		Random ran = new Random();
		for (int i = 0; i < 1000; i++) {
			
			Reviewer reviewer = new Reviewer();

			// 생년월일
			int birthYYYY = 2020-ran.nextInt(70)-13;
			
			Calendar birthDay = Calendar.getInstance();
			birthDay.set(Calendar.YEAR, birthYYYY);
			birthDay.add(Calendar.DATE, (ran.nextInt(365)+1)*-1);
			String strBirthDay = simpleDateFormat.format(birthDay.getTime());
			
			reviewer.setBirthDay(strBirthDay);

			reviewer.setNickNm("리뷰어"+(i+1)+"_"+strBirthDay);
			reviewer.setLvl(ran.nextInt(10)+1+"");
			
			String[] _sex = {"F", "M"};
			reviewer.setSex(_sex[ran.nextInt(2)]);
			
			reviewer.setSkinToneCd("SX0"+ran.nextInt(4));
			reviewer.setSkinTypeCd("SZ0"+ran.nextInt(8));
			
			if(i%10 != 0){
				String[] _skinTrub = {"SW0"+(ran.nextInt(6)+1), "SW0"+(ran.nextInt(6)+7)};
				reviewer.setSkinTrublList(_skinTrub);
			}
			
			String[] _skinEtc = {"SY13", "SY21", "SY23"};
			reviewer.setSkinEtcInfo(_skinEtc[ran.nextInt(3)]);
			
			reviewer.setProfileImg("/img/reviewer/reviewerPic"+(ran.nextInt(10)+1)+"_on.png");
			
			Calendar calendar = Calendar.getInstance();
			
			//등록일자는 무작위로
			calendar.add(Calendar.DATE, (ran.nextInt(365)+1)*-1);
			reviewer.setRegDate(calendar.getTime());
			reviewer.setUpdDate(calendar.getTime());

			reviewerRepository.save(reviewer);
			
		}

		int i = 0;
		List<Reviewer> reviewerList = reviewerRepository.findAll();
		for (Reviewer reviewer : reviewerList) {
			i++;
			
			int loopCnt = ran.nextInt(10); // 1명당 리뷰 등록 건수
			for (int x = 0; x < loopCnt; x++) {
				Review review = new Review();

				review.setReviewer_id(reviewer.get_id());
				
				String reviewCl = "";
				int num = ran.nextInt(2)+1;
				if (num == 1) {
					reviewCl = "A";
				} else {
					reviewCl = "B";
				}
	
				review.setReviewCl(reviewCl);

				String[] _prdSeq = {"11266"
						, "12068"
						, "12280"
						, "12416"
						, "12878"
						, "13059"
						, "13316"
						, "13317"
						, "13842"
						, "14397"
						, "14769"
						, "14771"
						, "14772"
						, "14774"
						, "14997"
						, "15084"
						, "15598"
						, "16017"
						, "16056"
						, "16377"
						, "16761"
						, "16762"
						, "16896"
						, "16957"
						, "16958"
						, "16996"
						, "17076"
						, "17617"
						, "17618"
						, "17660"
						, "17836"
						, "17840"
						, "18021"
						, "18218"
						, "18466"
						, "18600"
						, "19836"
						, "20256"
						, "20260"
						, "20360"
						, "20416"
						, "20418"
						, "20440"
						, "20476"
						, "20556"
						, "20576"
						, "20680"
						, "20856"
						, "20858"
						, "20860"
						, "20862"
						, "21856"
						, "21858"
						, "22536"
						, "22596"
						, "22636"
						, "22856"
						, "22858"
						, "23156"
						, "23956"
						, "24042"
						, "24078"
						, "24098"
						, "24588"
						, "25080"
						, "25256"
						, "25258"
						, "25296"
						, "25356"
						, "25416"
						, "25436"
						, "25456"
						, "25462"
						, "25482"
						, "25496"
						, "25502"
						, "25504"
						, "25506"
						, "25518"
						, "25542"
						, "25578"
						, "25636"
						, "25740"
						, "25742"
						, "25798"
						, "25800"
						, "25802"
						, "25804"
						, "25806"
						, "25816"
						, "25818"
						, "25916"
						, "25918"
						, "25920"
						, "25922"
						, "25936"
						, "25956"
						, "25996"
						, "25998"
						, "26076"
						, "26096"
						, "26120"
						, "26136"
						, "26138"
						, "26288"
						, "26316"
						, "26318"
						, "26322"
						, "26416"};
				review.setPrdSeq(_prdSeq[ran.nextInt(109)]);
				review.setBestFl((ran.nextInt(15)+1) > 10 && "A".equals(reviewCl)? "Y" : "N");
				review.setEvalScore(Integer.valueOf(ran.nextInt(5)+1));
				review.setHit(Integer.valueOf(ran.nextInt(1000)+1));
				review.setRecomCnt(Integer.valueOf(ran.nextInt(15)+1));
	//			review.setCmtCnt("");
				review.setRecbScore(Integer.valueOf(ran.nextInt(10)+1));
				
				String[] _goodCnts = {"색이 많이 진하네요\n생각보다 별로에요"
									, "가성비 좋네요\n많이파세요~~~ Good!"
									, "간편해서 좋아요 예비용으로 하나씩 파우치에 넣고다니는걸 추천합니다"
									, "저 이거 완전 좋아요!\n피부가 예민하구 해서 선크림 잘 못 고르는데 이게 완전 순하고 톤업기능듀 좋아서 요거 하나면 화장 끝이네요!!\n어느정도 커버 능력도 전 있는거 같았어요!\n좋네요! 원래 쓰던 제품도 유명한 톤업 선크림이었는데 성분도 더 좋고 발림감 뭐하나 떨어지는것도 없어요!\n가격차이는 많이 나는뎅 ㅎㅎ여기로 정착할렵니닽 할인이나 이벤트 많이 해주세요"};
				
				review.setGoodCnts("내용다름표기>> "+i+" "+_goodCnts[ran.nextInt(4)]);
				
				String[] _etcCnts = {""
						, ""
						, ""
						, ""
						, "옴청좋네요!"
						, "너무 좋아서 지인한테 선물하려고 또 구매했어요!"
						, "또사러올게요~"
						, ""};
				
				review.setEtcCnts("기타다름표기>>"+i+" "+_etcCnts[ran.nextInt(8)]);
	//			review.setTplRegCnt("");
	//			review.setPrevImg("");
				
				String[] _tpList = null;
				if("A".equals(review.getReviewCl())) {
					_tpList = new String[(ran.nextInt(5)+1)];
					
					boolean flag;
					int idx = 0;
					while (true) {
						flag = false;
						num = (ran.nextInt(10)+1);
						for(int y = 0; y < _tpList.length; y++) {
							//실제 경로에 맞춰 수정(reviews -> review)
							if (_tpList[y] != null && _tpList[y].equals("/img/review/"+num+".jpg")) {
								flag = true;
							}
						}
						
						if (!flag) {
							//실제 경로에 맞춰 수정(reviews -> review)
							_tpList[idx++] = "/img/review/"+num+".jpg";
							
							if (idx == _tpList.length) {
								break;
							}
						}
					}
				}
				
				review.setTplList(_tpList);
			
				Calendar calendar = Calendar.getInstance();
				
				//등록일자는 무작위로
				calendar.add(Calendar.DATE, (ran.nextInt(365)+1)*-1);
				review.setRegDate(calendar.getTime());
				review.setUpdDate(calendar.getTime());
				
				reviewRepository.save(review);

			}
			
		}
		
		ObjectId reviewer_id;
		List<Review> reviewList = reviewRepository.findAll();
		for (Review review : reviewList) {
			
			i = 0;
			int commentCnt = ran.nextInt(20)+1;
			reviewer_id = review.getReviewer_id();
			
			Date regDate = review.getRegDate();
			Calendar compareDate = Calendar.getInstance();
			compareDate.setTime(regDate);
			
			Calendar today = Calendar.getInstance();
			for (Reviewer reviewer : reviewerList) {
				Comment comment = new Comment();
				
				if (reviewer_id.equals(reviewer.get_id())) {
					continue;
				}

				if ((ran.nextInt(3)+1)%3 == 0) {
					i++;

					comment.setReview_id(review.get_id());
					comment.setReviewer_id(reviewer.get_id());
					comment.setCnts(i+"등");
					
					compareDate.add(Calendar.MINUTE, (ran.nextInt(60)+1));
					
					// 미래날짜로 등록되지 않도록함
					if (compareDate.compareTo(today) == 1) {
						break;
					}
					comment.setRegDate(compareDate.getTime());
					comment.setUpdDate(compareDate.getTime());
					
					commentRepository.save(comment);

					if (commentCnt <= i) {
						break;
					}
				}
			}
		}
	}
}
