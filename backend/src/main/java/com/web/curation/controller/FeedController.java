package com.web.curation.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.web.curation.model.entity.Alarm;
import com.web.curation.model.entity.FeedDto;
import com.web.curation.model.repository.AlarmRepository;
import com.web.curation.model.repository.UserInfoRepository;

@CrossOrigin(origins = { "http://localhost:3000" })
@RestController
public class FeedController {

	@Autowired
	AlarmRepository alarmRepository;
	
	@Autowired
	UserInfoRepository userInfoRepository;
	
	@PostMapping("/getFeeds")
	public List<FeedDto> getFeeds(@RequestParam(required=true) final String email){
		int auser=userInfoRepository.findByEmail(email).getUno();
		List<FeedDto> list=new ArrayList<>();
		List<Alarm> tmpList=alarmRepository.findByAuserAndAtype(auser, 1,Sort.by("ano").descending());
		for(Alarm alarm:tmpList) {
			FeedDto dto=new FeedDto();
			dto.setAno(alarm.getAno());
			dto.setAurl(alarm.getAurl());
			dto.setAsummary(alarm.getAsummary());
			dto.setAdate(alarm.getAdate());
			dto.setCreateUser(alarm.getCreateUser());
			dto.setCreateUserName(userInfoRepository.findById(alarm.getCreateUser()).get().getUname());
			dto.setProfileImg("");
			dto.setProfileImg(userInfoRepository.findById(alarm.getCreateUser()).get().getUprofileImg());
			
			list.add(dto);
		}
		System.out.println(list);
		return list;
	}
	
	
}
