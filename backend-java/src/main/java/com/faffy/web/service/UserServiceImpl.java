package com.faffy.web.service;

import com.faffy.web.dto.*;
import com.faffy.web.exception.DataIntegrityException;
import com.faffy.web.exception.DataNotFoundException;
import com.faffy.web.exception.ExceptionMsg;
import com.faffy.web.exception.IllegalInputException;
import com.faffy.web.jpa.entity.Consulting;
import com.faffy.web.jpa.entity.ConsultingLog;
import com.faffy.web.jpa.entity.UploadFile;
import com.faffy.web.jpa.entity.User;
import com.faffy.web.jpa.repository.ConsultingLogRepository;
import com.faffy.web.jpa.repository.UploadFileRepository;
import com.faffy.web.jpa.repository.UserRepository;
import com.faffy.web.jpa.type.PublicUserInfo;
import com.faffy.web.jpa.type.RegularExpression;
import com.faffy.web.service.file.FileHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.faffy.web.exception.ExceptionMsg.*;
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UploadFileRepository uploadFileRepository;
    @Autowired
    ConsultingLogRepository consultingLogRepository;
    @Autowired
    FileHandler fileHandler;

    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate redisTemplate;

    @Override
    public User getUserByNo(int no) throws Exception {
        return userRepository.findByNo(no).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));
    }

    @Override
    public User getUserByNickname(String nickname) throws Exception {
        return userRepository.findByNickname(nickname).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));
    }

    @Override
    public User getUserByEmail(String email) throws Exception{
        return userRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException(USER_NOT_FOUND_MSG));
    }

    @Override
    public List<PublicUserInfo> findAllUsers() {
        return userRepository.findAllBy();
    }


    @Override
    public void deleteUser(int no) throws Exception {
        try {
            userRepository.deleteById(no);
        } catch (Exception e) {
            throw new Exception(USER_NOT_FOUND_MSG);
        }
    }


    @Override
    public UserPublicDto login(UserLoginDto userDto) throws IllegalArgumentException {
        User user = userRepository.findByEmail(userDto.getEmail()).orElseThrow(()->new IllegalArgumentException(USER_NOT_FOUND_MSG));

        if(!passwordEncoder.matches(userDto.getPassword(),user.getPassword())) {
            throw new IllegalArgumentException(LOGIN_FAILED_MSG);
        }
        return user.toPublicDto();
    }

    @Override
    public void logout(String token) {
        long duration = 30 * 60 *1000;
        final String PREFIX = "logout";
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(PREFIX + token, token, expireDuration);

    }

    @Override
    public UserGetDetailDto getProfile(int no) {
        User user = userRepository.findByNo(no).orElse(null);
        if(user == null)
            return null;

        return user.toDetailDto();
    }

    @Override
    public File getProfileImg(int no) {
        User user = userRepository.findByNo(no).orElse(null);
        if(user == null)
            return null;

        UploadFile uf = user.getProfileImage();
        String filename = uf.getUploadPath() + File.separator + uf.getUuid() + "_" + uf.getFileName();
        return new File(filename);
    }

    @Override
    public List<BroadCastHistoryDto> getPartiList(int no) {
        User user = userRepository.findByNo(no).orElse(null);
        if(user == null)
            return null;

        List<ConsultingLog> logList = consultingLogRepository.findConsultingLogsByUserNo(no).orElse(null);
        List<Consulting> consultings = new ArrayList<>();
        List<BroadCastHistoryDto> dtoList = new ArrayList<>();
        if(!logList.isEmpty()){
            for(ConsultingLog log : logList){
                Consulting consulting = log.getConsulting();
                if(consulting.getConsultant().getNo() == user.getNo()) continue; // 방송 진행한 경우는 건너뜀
                consultings.add(consulting);
            }

            for(Consulting consulting : consultings){
                Date date = Timestamp.valueOf(consulting.getStartTime());
                String sdate = date.toString().split(" ")[0].replace(':', '-');
                BroadCastHistoryDto dto = BroadCastHistoryDto.builder()
                        .consultant(consulting.getConsultant().getNickname())
                        .title(consulting.getTitle())
                        .date(sdate)
                        .intro(consulting.getIntro())
                        .build();
                dtoList.add(dto);
            }
        }

        return dtoList;
    }

    @Override
    public List<BroadCastHistoryDto> getConsultList(int no) {
        User user = userRepository.findByNo(no).orElse(null);
        if(user == null)
            return null;

        List<ConsultingLog> logList = consultingLogRepository.findConsultingLogsByUserNo(no).orElse(null);
        List<Consulting> consultings = new ArrayList<>();
        List<BroadCastHistoryDto> dtoList = new ArrayList<>();
        if(!logList.isEmpty()){
            for(ConsultingLog log : logList){
                Consulting consulting = log.getConsulting();
                if(consulting.getConsultant().getNo() != user.getNo()) continue; // 방송 참여한 경우는 건너뜀
                consultings.add(consulting);
            }

            for(Consulting consulting : consultings){
                Date date = Timestamp.valueOf(consulting.getStartTime());
                String sdate = date.toString().split(" ")[0].replace(':', '-');
                BroadCastHistoryDto dto = BroadCastHistoryDto.builder()
                        .consultant(consulting.getConsultant().getNickname())
                        .title(consulting.getTitle())
                        .date(sdate)
                        .intro(consulting.getIntro())
                        .build();
                dtoList.add(dto);
            }
        }

        return dtoList;
    }

    @Override
    public User addUser(UserDto userDto) throws IllegalInputException, DataIntegrityException {
        try {
            userDto.setRoles(Collections.singletonList("ROLE_USER"));
            userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
            return userRepository.save(userDto.toEntity());
        } catch (IllegalInputException e) {
            throw new IllegalInputException(ILLEGAL_INPUT_MSG);
        } catch (Exception e) {
            throw new DataIntegrityException(DUPLICATED_MSG);
        }
    }

    @Override
    @Transactional
    public User updateUser(UserDto userDto) throws DataNotFoundException, IllegalInputException {
        // 비밀번호 정규 표현식 만족 체크
        if (!Pattern.matches(RegularExpression.PASSWORD_REG_EX,userDto.getPassword()))
            throw new IllegalInputException(ILLEGAL_PASSWORD_CONDITION);
        
        try {
            User user;
            if (userDto.getNo() == 0) {
                user = userRepository.findByEmail(userDto.getEmail()).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));
            } else {
                user = userRepository.findByNo(userDto.getNo()).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));
            }
            
            userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
            //프로필 사진 관련
            MultipartFile file = userDto.getFile();
            if(file != null){ //선택한 파일이 있으면 이전 프로필 사진 삭제
                System.out.println("===file is not Null===");
                UploadFile exImg = user.getProfileImage();
                if(exImg != null) {
                    uploadFileRepository.delete(exImg);
                    if (!fileHandler.deleteFile(exImg))
                        System.out.println("------파일 삭제 실패------");
                }

                UploadFile img = fileHandler.parseFileInfo(file);
                if(img != null){
                    uploadFileRepository.save(img);
                    user.updateProfileImage(img);
                }
            }
            else{ //선택한 파일이 없는 경우 기존 프로필 사진과 db 정보 삭제
                System.out.println("===No File selected===");
                UploadFile img = user.getProfileImage();
                user.updateProfileImage(null);
                if(img != null) {
                    uploadFileRepository.delete(img); //db에서 프로필 이미지 저장 정보 삭제
                    fileHandler.deleteFile(img); //실제 파일 삭제
                }
            }

            user.updateUser(userDto);
            return user;
        } catch (IllegalArgumentException e) {
            throw new DataNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new IllegalInputException(ILLEGAL_INPUT_MSG);
        }
    }

//    @Override
//    @Transactional
//    public User updateUserImg(MultipartFile file) throws DataNotFoundException, IllegalInputException{
//        User user = userRepository.findByNo(1).orElse(null);
//        if(file == null || user == null)
//            return null;
//
//        try {
//            UploadFile img = fileHandler.parseFileInfo(file);
//            if (img != null) {
//                uploadFileRepository.save(img);
//                user.updateProfileImage(img);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return user;
//    }


}
