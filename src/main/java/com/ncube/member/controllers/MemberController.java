package com.ncube.member.controllers;

import com.ncube.member.MemberRequestException;
import com.ncube.member.model.Member;
import com.ncube.member.repositories.MemberRepository;
import com.ncube.member.services.ImagesStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@RestController()
@Api(value = "members", description = "Members management")
@RequestMapping("members")
public class MemberController {

    private final MemberRepository memberRepository;
    private final ImagesStoreService<MultipartFile> imagesStoreService;

    @Autowired
    public MemberController(MemberRepository memberRepository, ImagesStoreService<MultipartFile> imagesStoreService) {
        this.memberRepository = memberRepository;
        this.imagesStoreService = imagesStoreService;
    }

    @ApiOperation(value = "post member", response = ResponseEntity.class)
    @PostMapping
    public ResponseEntity<Object> createMember(@RequestPart("member") Member member,
                                               @RequestPart("image") MultipartFile imageFile) {
        String memberId = memberRepository.save(member).getId();
        return saveMember(member, imageFile, memberId);
    }

    @ApiOperation(value = "put member", response = ResponseEntity.class)
    @PutMapping("{id}")
    public ResponseEntity<Object> updateMember(@PathVariable String id, @RequestPart("member") Member member,
                                               @RequestPart("image") MultipartFile imageFile) throws Throwable {
        Member updatingMember = memberRepository.findById(id).orElseThrow(getNoIdExceptionSupplier(id));

        imagesStoreService.deleteFile(updatingMember.getImageUrl());

        member.setId(id);
        return saveMember(member, imageFile, id);
    }

    private String validateImageAndGetExtension(String contentType) {
        String[] fileTypeAndExtension = contentType.split("/");
        if (!fileTypeAndExtension[0].equals("image")) {
            throw new MemberRequestException("Only images are acceptable as member picture");
        }
        return fileTypeAndExtension[1];
    }

    private ResponseEntity<Object> saveMember(Member member, MultipartFile imageFile, String memberId) {
        String fileExtension = validateImageAndGetExtension(Objects.requireNonNull(imageFile.getContentType()));
        String imageUrl = imagesStoreService.uploadFile(imageFile, String.format("Member_%s.%s", memberId, fileExtension));
        member.setImageUrl(imageUrl);

        memberRepository.save(member);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(memberId).toUri();

        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "get members", response = List.class)
    @GetMapping
    public List<Member> listMembers() {
        return memberRepository.findAll();
    }

    @ApiOperation(value = "get member", response = Member.class)
    @GetMapping("/{id}")
    public Member getMember(@PathVariable String id) throws Throwable {
        return memberRepository.findById(id).orElseThrow(getNoIdExceptionSupplier(id));
    }

    @ApiOperation(value = "delete member", response = ResponseEntity.class)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteMember(@PathVariable String id) throws Throwable {
        Member memberToDelete = memberRepository.findById(id).orElseThrow(getNoIdExceptionSupplier(id));

        imagesStoreService.deleteFile(memberToDelete.getImageUrl());

        memberRepository.delete(memberToDelete);

        return ResponseEntity.accepted().build();
    }

    private Supplier<Throwable> getNoIdExceptionSupplier(String id) {
        return () -> new MemberRequestException(String.format("No member with id %s found", id));
    }

    public @ExceptionHandler({MemberRequestException.class})
    void handleBadRequests(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

}
