package com.web.curation.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.web.curation.model.entity.Alarm;
import com.web.curation.model.entity.Category;
import com.web.curation.model.entity.FriendInfo;
import com.web.curation.model.entity.GroupApply;
import com.web.curation.model.entity.GroupInfo;
import com.web.curation.model.entity.GroupParticipant;
import com.web.curation.model.entity.UserInfo;
import com.web.curation.model.repository.AlarmRepository;
import com.web.curation.model.repository.CategoryRepository;
import com.web.curation.model.repository.FriendInfoRepository;
import com.web.curation.model.repository.GroupApplyRepository;
import com.web.curation.model.repository.GroupInfoRepository;
import com.web.curation.model.repository.GroupParticipantRepository;
import com.web.curation.model.repository.UserInfoRepository;

@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RestController
public class GroupController {

	@Autowired
	UserInfoRepository userInfoRepository;

	@Autowired
	GroupInfoRepository groupInfoRepository;

	@Autowired
	GroupParticipantRepository groupParticipantRepository;

	@Autowired
	AlarmRepository alarmRepository;

	@Autowired
	FriendInfoRepository friendInfoRepository;

	@Autowired
	GroupApplyRepository groupApplyRepository;
	
	@Autowired
	CategoryRepository categoryRepository;

	@PostMapping("/getCategory")
	public Object getCategory() {
		Map<String,Object> resultMap=new HashMap<>();
		
		List<Category> list= categoryRepository.findAll();
		resultMap.put("list",list);
		
		return resultMap;
	}
	
	@PostMapping("/getUserListInGroup")
	public Object getUserListInGroup(@RequestParam int gno) {
		Map<String, Object> resultMap = new HashMap<>();

		List<GroupParticipant> list = groupParticipantRepository.findAllByGno(gno);
		List<Integer> unoList = new ArrayList<>();
		for (GroupParticipant gp : list)
			unoList.add(gp.getUno());

		List<UserInfo> userList = userInfoRepository.findAllByUnoIn(unoList);

		resultMap.put("userList", userList);

		return resultMap;
	}

	@PostMapping("/getGroupList")
	public Object getGroupList(@RequestParam String email) {
		Map<String, Object> resultMap = new HashMap<>();

		UserInfo userInfo = userInfoRepository.findByEmail(email);
		List<GroupParticipant> list = groupParticipantRepository.findAllByUno(userInfo.getUno());

		List<Integer> gnoList = new ArrayList<>();

		for (GroupParticipant gp : list)
			gnoList.add(gp.getGno());

		List<GroupInfo> groupList = groupInfoRepository.findAllByGnoIn(gnoList);
		resultMap.put("groupList", groupList);
		
		return resultMap;
	}

	@PostMapping("/makeGroup")
	public Object makeGroup(@RequestParam String email, @RequestParam String gname, @RequestParam int gcategory,
			@RequestParam int gboundary) {
		Map<String, Object> resultMap = new HashMap<>();
		UserInfo myInfo=userInfoRepository.findByEmail(email);
		int gmaster = myInfo.getUno();

		GroupInfo groupInfo = new GroupInfo();
		groupInfo.setGboundary(gboundary);
		groupInfo.setGcategory(gcategory);
		groupInfo.setGmaster(gmaster);
		groupInfo.setGname(gname);
		groupInfo.setGuserList(Integer.toString(gmaster)+" ");

		groupInfo=groupInfoRepository.save(groupInfo);
		GroupParticipant groupParticipant=new GroupParticipant();
		groupParticipant.setGno(groupInfo.getGno());
		groupParticipant.setUno(gmaster);
		groupParticipantRepository.save(groupParticipant);

		
		if(gboundary!=0) {
			List<FriendInfo> friendList=getFriendList(gmaster);
			StringBuilder sb=new StringBuilder();
			sb.append("회원님의 친구 ");
			sb.append(myInfo.getUname());
			sb.append("님이 ");
			sb.append(gname);
			sb.append("그룹을 만들었습니다.");
			for(FriendInfo fi:friendList) {
				Alarm alarm=new Alarm();
				alarm.setAtype(1);
				alarm.setAurl("#");
				alarm.setAuser(fi.getMyId());
				alarm.setCreateUser(gmaster);
				alarm.setAsummary(sb.toString());
				
				alarmRepository.save(alarm);
			}
			if(gboundary==2) {
				//임찬규 박봉현 님의 친구인 이기호님이 그룹 만들었습니다.
				//친구의 친구를 세트에 담는다
				//세트에서 내 친구들과 나 제외
				//친구의친구 수만큼 반복문
				//내 친구와 해당 친구의 친구의 공통 친구들을 찾는다
				List<Integer> friendFriendList=new ArrayList<>();
				List<Integer> friendIntList=new ArrayList<>();
				friendIntList.add(gmaster);
				for(FriendInfo fi:friendList) {
					friendIntList.add(fi.getMyId());
					List<FriendInfo> toFindFriendFriend=getFriendList(fi.getMyId());
					for(FriendInfo friendFriend:toFindFriendFriend) {
						friendFriendList.add(friendFriend.getMyId());
					}
				}
				HashSet<Integer> tmp=new HashSet<>(friendFriendList);
				friendFriendList=new ArrayList<>(tmp);
				friendFriendList.removeAll(friendIntList);
				
				for(Integer ff:friendFriendList) {
					
					StringBuilder sb2=new StringBuilder();
					for(Integer f:friendIntList) {
						if(isFriendFriend(gmaster,f,ff)) {
							sb2.append(userInfoRepository.findById(f).get().getUname());
							sb2.append(" ");
						}
					}
					if(sb2.length()==0)
						continue;
					sb2.append("님의 친구 ");
					sb2.append(myInfo.getUname());
					sb2.append("님이 ");
					sb2.append(gname);
					sb2.append("그룹을 만들었습니다.");
					String asummary=sb2.toString();

					Alarm alarm=new Alarm();
					alarm.setAuser(ff);
					alarm.setAurl("#");
					alarm.setCreateUser(gmaster);
					alarm.setAtype(1);
					alarm.setAsummary(asummary);
					
					alarmRepository.save(alarm);
				}
			}
		}

		resultMap.put("data", "그룹 생성에 성공했습니다.");
		return resultMap;
	}

	@PostMapping("/delGroup")
	public Object delGroup(@RequestParam int gno) {
		Map<String, Object> resultMap = new HashMap<>();

		GroupInfo groupInfo = groupInfoRepository.findById(gno).get();
		groupInfoRepository.delete(groupInfo);

		resultMap.put("data", "그룹 삭제에 성공했습니다.");

		return resultMap;
	}

	@PostMapping("/changeGroupMaster")
	public Object changeGroupMaster(@RequestParam int gno, @RequestParam int nextMaster) {
		Map<String, Object> resultMap = new HashMap<>();

		GroupInfo groupInfo = groupInfoRepository.findById(gno).get();
		groupInfo.setGmaster(nextMaster);
		groupInfoRepository.save(groupInfo);

		resultMap.put("data", "그룹장 변경에 성공했습니다.");
		return resultMap;
	}

	@PostMapping("/inviteGroup")
	public Object inviteGroup(@RequestParam String email, @RequestParam int friendId, @RequestParam int gno) {
		Map<String, Object> resultMap = new HashMap<>();

		UserInfo myInfo = userInfoRepository.findByEmail(email);
		GroupInfo groupInfo = groupInfoRepository.findById(gno).get();
		Alarm alarm = new Alarm();

		StringBuilder sb = new StringBuilder();
		sb.append(myInfo.getUname());
		sb.append("님이 ");
		sb.append(groupInfo.getGname());
		sb.append("그룹으로 초대하셨습니다!");
		alarm.setAsummary(sb.toString());
		alarm.setAuser(friendId);
		alarm.setCreateUser(myInfo.getUno());
		alarm.setAtype(0);
		alarm.setAurl("#");
		alarmRepository.save(alarm);

		GroupApply groupApply = new GroupApply();
		groupApply.setAisApply(false);
		groupApply.setGno(groupInfo.getGno());
		groupApply.setUno(friendId);

		groupApplyRepository.save(groupApply);

		return resultMap;
	}

	@PostMapping("/acceptInviteGroup")
	public Object acceptInviteGroup(@RequestParam String email, @RequestParam int gno) {
		Map<String, Object> resultMap = new HashMap<>();

		UserInfo myInfo = userInfoRepository.findByEmail(email);

		GroupApply groupApply = groupApplyRepository.findByUnoAndGno(myInfo.getUno(), gno).get();
		groupApplyRepository.delete(groupApply);

		GroupInfo groupInfo = groupInfoRepository.findById(gno).get();
		GroupParticipant groupParticipant = new GroupParticipant();
		groupParticipant.setGno(gno);
		groupParticipant.setUno(myInfo.getUno());
		groupParticipantRepository.save(groupParticipant);

		List<GroupParticipant> list = groupParticipantRepository.findAllByGno(gno);
		StringBuilder sb = new StringBuilder();
		for (GroupParticipant gp : list) {
			sb.append(gp.getUno());
			sb.append(" ");
		}
		
		List<FriendInfo> friendList=getFriendList(myInfo.getUno());
		
		StringBuilder sb2 = new StringBuilder();
		sb2.append(myInfo.getUname());
		sb2.append("님이 ");
		sb2.append(groupInfo.getGname());
		sb2.append("그룹에 가입하셨습니다.");
		for(FriendInfo fi:friendList) {
			Alarm alarm = new Alarm();
			alarm.setAtype(1);
			alarm.setAurl("#");
			alarm.setAuser(fi.getMyId());
			alarm.setCreateUser(myInfo.getUno());

			alarm.setAsummary(sb2.toString());
			alarmRepository.save(alarm);
		}

		groupInfo.setGuserList(sb.toString());
		groupInfoRepository.save(groupInfo);
		resultMap.put("data", "그룹에 가입했습니다!");

		return resultMap;
	}

	@PostMapping("/applyGroup")
	public Object joinGroup(@RequestParam String email, @RequestParam int gno) {
		Map<String, Object> resultMap = new HashMap<>();

		UserInfo myInfo = userInfoRepository.findByEmail(email);

		GroupApply groupApply = new GroupApply();
		groupApply.setAisApply(true);
		groupApply.setGno(gno);
		groupApply.setUno(myInfo.getUno());

		GroupInfo groupInfo = groupInfoRepository.findById(gno).get();

		Alarm alarm = new Alarm();
		alarm.setAtype(0);
		alarm.setAurl("#");
		alarm.setAuser(groupInfo.getGmaster());
		alarm.setCreateUser(myInfo.getUno());

		StringBuilder sb = new StringBuilder();
		sb.append(myInfo.getUname());
		sb.append("님이 ");
		sb.append(groupInfo.getGname());
		sb.append("그룹에 가입을 신청했습니다.");
		alarm.setAsummary(sb.toString());
		
		alarmRepository.save(alarm);

		resultMap.put("data", "그룹에 가입 신청을 보냈습니다.");

		return resultMap;
	}
	
	@PostMapping("/getGroupApplyList")
	public Object getGroupApplyList(@RequestParam int gno) {
		Map<String,Object> resultMap=new HashMap<>();
		
		List<GroupApply> list=null;
		if(groupApplyRepository.findAllByGno(gno).isPresent()) 
			list=groupApplyRepository.findAllByGno(gno).get();
		
		resultMap.put("applyList",list);
		
		return resultMap;
	}

	@PostMapping("/acceptApplyGroup")
	public Object acceptJoinoinGroup(@RequestParam int uno, @RequestParam int gno) {
		Map<String, Object> resultMap = new HashMap<>();

		GroupApply groupApply = groupApplyRepository.findByUnoAndGno(uno, gno).get();
		groupApplyRepository.delete(groupApply);

		GroupInfo groupInfo = groupInfoRepository.findById(gno).get();

		GroupParticipant groupParticipant = new GroupParticipant();
		groupParticipant.setGno(gno);
		groupParticipant.setUno(uno);
		groupParticipantRepository.save(groupParticipant);

		List<GroupParticipant> gpList = groupParticipantRepository.findAllByGno(gno);

		StringBuilder sb = new StringBuilder();
		for (GroupParticipant gp : gpList) {
			sb.append(gp.getUno());
			sb.append(" ");
		}
		groupInfo.setGuserList(sb.toString());
		groupInfoRepository.save(groupInfo);

		resultMap.put("data", "가입 요청을 승인했습니다.");

		return resultMap;
	}

	@PostMapping("/getoutGroup")
	public Object getoutGroup(@RequestParam int gno, @RequestParam String email) {
		Map<String, Object> resultMap = new HashMap<>();

		UserInfo myInfo = userInfoRepository.findByEmail(email);
		GroupInfo groupInfo = groupInfoRepository.findById(gno).get();

		GroupParticipant groupParticipant = groupParticipantRepository.findByUnoAndGno(myInfo.getUno(), gno).get();
		groupParticipantRepository.delete(groupParticipant);

		List<GroupParticipant> gpList = groupParticipantRepository.findAllByGno(gno);

		StringBuilder sb = new StringBuilder();
		for (GroupParticipant gp : gpList) {
			sb.append(gp.getUno());
			sb.append(" ");
		}
		groupInfo.setGuserList(sb.toString());
		groupInfoRepository.save(groupInfo);

		resultMap.put("data", "그룹에서 탈퇴했습니다.");

		return resultMap;
	}

	@PostMapping("/banishGroup")
	public Object banishGroup(@RequestParam int gno, @RequestParam int uno) {
		Map<String, Object> resultMap = new HashMap<>();

		GroupInfo groupInfo = groupInfoRepository.findById(gno).get();

		GroupParticipant groupParticipant = groupParticipantRepository.findByUnoAndGno(uno, gno).get();
		groupParticipantRepository.delete(groupParticipant);

		List<GroupParticipant> gpList = groupParticipantRepository.findAllByGno(gno);

		StringBuilder sb = new StringBuilder();
		for (GroupParticipant gp : gpList) {
			sb.append(gp.getUno());
			sb.append(" ");
		}
		groupInfo.setGuserList(sb.toString());
		groupInfoRepository.save(groupInfo);

		resultMap.put("data", "그룹에서 추방했습니다.");

		return resultMap;
	}
	
	@PostMapping("/groupJoinStatus")
	public Object groupJoinStatus(int uno, int gno) {
		Map<String,Object> resultMap=new HashMap<>();
		StringBuilder sb=new StringBuilder();
		int status;//0:비회원, 1:가입신청상태, 2:초대받은상태, 3:회원
		
		if(groupParticipantRepository.findByUnoAndGno(uno, gno).isPresent()) {
			sb.append("회원");
			status=3;
		}
		else if(groupApplyRepository.findByUnoAndGno(uno, gno).isPresent()) {
			if(groupApplyRepository.findByUnoAndGno(uno, gno).get().isAisApply()) {
				status=1;
				sb.append("가입신청한상태");
			}
			else {
				status=2;
				sb.append("초대받은상태");
			}
		}
		else {
			sb.append("회원아님");
			status=0;
		}
		resultMap.put("message",sb.toString());
		resultMap.put("joinStatus",status);
		
		return resultMap;
	}

	public List<FriendInfo> getFriendList(int uno){
		Optional<List<FriendInfo>> friendList = friendInfoRepository.findAllByMyId(uno);

		List<FriendInfo> toReturnFriendList=new ArrayList<>();
		if (friendList.isPresent()) {
			for (FriendInfo friendInfo : friendList.get()) {
				Optional<FriendInfo> fi = friendInfoRepository.findByMyIdAndFriendId(friendInfo.getFriendId(),
						uno);
				if (fi.isPresent()) 
					toReturnFriendList.add(fi.get());
				
			}
		}
		
		return toReturnFriendList;
	}

	public boolean isFriendFriend(int id1, int bridge, int id2) {
		if (friendInfoRepository.findByMyIdAndFriendId(id1, bridge).isPresent()
				&& friendInfoRepository.findByMyIdAndFriendId(bridge, id1).isPresent()
				&& friendInfoRepository.findByMyIdAndFriendId(id2, bridge).isPresent()
				&& friendInfoRepository.findByMyIdAndFriendId(bridge, id2).isPresent())
			return true;
		return false;

	}	
}