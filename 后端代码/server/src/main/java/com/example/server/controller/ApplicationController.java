package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.*;
import com.example.server.mapper.*;
import com.example.server.service.OssService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 申请提交与审核控制器
 * 用户端提交附件→OSS，生成不同阶段的申请；
 * 支部管理员先审，超级管理员终审。
 */
@RestController
public class ApplicationController {

	@Autowired
	private PositiveMemberApplicationMapper positiveMapper;
	@Autowired
	private PartyApplicationMapper partyMapper;
	@Autowired
	private PrepareMemberApplicationMapper prepareMapper;
	@Autowired
	private ApplicationProcessMapper processMapper;
	@Autowired
	private AdminMapper adminMapper;
	@Autowired
	private OssService ossService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MemberMapper memberMapper;

	@PostMapping("/files/upload")
	public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
		try {
			String key = "applications/" + UUID.randomUUID() + "/" + Objects.requireNonNull(file.getOriginalFilename());
			String url = ossService.upload(key, file.getInputStream());
			Map<String, String> resp = new HashMap<>();
			resp.put("url", url);
			resp.put("objectKey", key);
			return Result.success(resp);
		} catch (Exception e) {
			return Result.error(500, "上传失败: " + e.getMessage());
		}
	}

    @PostMapping("/user/applications/positive")
    public Result<Long> applyPositive(HttpServletRequest request, @RequestBody Map<String, Object> body) {
		Long userId = (Long) request.getAttribute("userId");
		if (userId == null) return Result.error(401, "未登录");
        String details = body == null ? null : String.valueOf(body.getOrDefault("details", ""));
        @SuppressWarnings("unchecked")
        List<String> attachments = body == null ? null : (List<String>) body.get("attachments");
		PositiveMemberApplication entity = new PositiveMemberApplication();
		entity.setUserId(userId);
		entity.setApplicationDetails(details);
        entity.setSupportingDocuments(attachments == null ? null : toJsonString(attachments));
        entity.setStatus("PENDING");
        entity.setReviewerId(getDefaultReviewerId());
		positiveMapper.insert(entity);
		upsertProcess(userId, "POSITIVE_MEMBER");
		return Result.success(entity.getId());
	}

    @PostMapping("/user/applications/party")
    public Result<Long> applyParty(HttpServletRequest request, @RequestBody Map<String, Object> body) {
		Long userId = (Long) request.getAttribute("userId");
		if (userId == null) return Result.error(401, "未登录");
        String details = body == null ? null : String.valueOf(body.getOrDefault("details", ""));
        @SuppressWarnings("unchecked")
        List<String> attachments = body == null ? null : (List<String>) body.get("attachments");
		PartyApplication entity = new PartyApplication();
		entity.setUserId(userId);
		entity.setApplicationDetails(details);
        // 复用 training_records 字段存储附件URL JSON
        entity.setTrainingRecords(attachments == null ? null : toJsonString(attachments));
        entity.setStatus("PENDING");
        entity.setReviewerId(getDefaultReviewerId());
		partyMapper.insert(entity);
		upsertProcess(userId, "PARTY_APPLICATION");
		return Result.success(entity.getId());
	}

    @PostMapping("/user/applications/prepare")
    public Result<Long> applyPrepare(HttpServletRequest request, @RequestBody Map<String, Object> body) {
		Long userId = (Long) request.getAttribute("userId");
		if (userId == null) return Result.error(401, "未登录");
        String evaluationReport = body == null ? null : String.valueOf(body.getOrDefault("evaluationReport", ""));
        String probationStart = body == null ? null : String.valueOf(body.getOrDefault("probationStart", ""));
        String probationEnd = body == null ? null : String.valueOf(body.getOrDefault("probationEnd", ""));
        @SuppressWarnings("unchecked")
        List<String> attachments = body == null ? null : (List<String>) body.get("attachments");
		PrepareMemberApplication entity = new PrepareMemberApplication();
		entity.setUserId(userId);
        // 将报告与附件包装为JSON，避免变更表结构
        if (attachments != null && !attachments.isEmpty()) {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("text", evaluationReport);
            payload.put("attachments", attachments);
            entity.setEvaluationReport(toJsonString(payload));
        } else {
            entity.setEvaluationReport(evaluationReport);
        }
		entity.setProbationPeriodStart(LocalDateTime.parse(probationStart));
		entity.setProbationPeriodEnd(LocalDateTime.parse(probationEnd));
        entity.setStatus("PENDING");
        entity.setReviewerId(getDefaultReviewerId());
		prepareMapper.insert(entity);
		upsertProcess(userId, "PREPARE_MEMBER");
		return Result.success(entity.getId());
	}

    /**
     * 用户端：获取当前用户的申请与审核记录
     */
    @GetMapping("/user/applications/history")
    public Result<List<Map<String, Object>>> myHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "未登录");
        List<Map<String, Object>> out = new ArrayList<>();
        Member me = memberMapper.selectById(userId);
        for (PositiveMemberApplication a : safeList(positiveMapper.selectByUserId(userId))) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "积极分子申请");
            m.put("status", mapStatus(a.getStatus()));
            m.put("reviewedAt", a.getReviewedAt());
            // 申请人信息
            if (me != null) {
                m.put("name", me.getUsername());
                m.put("studentId", me.getStudentId());
            }
            // 提交时间
            ApplicationProcess pr = processMapper.selectByUserAndStage(userId, "POSITIVE_MEMBER");
            m.put("submittedAt", pr == null ? null : pr.getSubmittedAt());
            out.add(m);
        }
        for (PartyApplication a : safeList(partyMapper.selectByUserId(userId))) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "入党申请");
            m.put("status", mapStatus(a.getStatus()));
            m.put("reviewedAt", a.getReviewedAt());
            if (me != null) {
                m.put("name", me.getUsername());
                m.put("studentId", me.getStudentId());
            }
            ApplicationProcess pr = processMapper.selectByUserAndStage(userId, "PARTY_APPLICATION");
            m.put("submittedAt", pr == null ? null : pr.getSubmittedAt());
            out.add(m);
        }
        for (PrepareMemberApplication a : safeList(prepareMapper.selectByUserId(userId))) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "预备党员申请");
            m.put("status", mapStatus(a.getStatus()));
            m.put("reviewedAt", a.getReviewedAt());
            if (me != null) {
                m.put("name", me.getUsername());
                m.put("studentId", me.getStudentId());
            }
            ApplicationProcess pr = processMapper.selectByUserAndStage(userId, "PREPARE_MEMBER");
            m.put("submittedAt", pr == null ? null : pr.getSubmittedAt());
            out.add(m);
        }
        // 可按时间排序
        out.sort((a,b) -> String.valueOf(b.get("reviewedAt")).compareTo(String.valueOf(a.get("reviewedAt"))));
        return Result.success(out);
    }

	/**
	 * 管理端获取申请汇总（简化：按三类合并列表）
	 */
	@GetMapping("/admin/applications")
	public Result<List<Map<String, Object>>> listAll(HttpServletRequest request) {
		Object userType = request.getAttribute("userType");
		if (userType == null) return Result.error(401, "未登录");
		String type = userType.toString();
		if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) return Result.error(403, "无权限");
		
		// 获取当前管理员的支部ID（如果是支部管理员）
		Long adminBranchId = null;
		if ("BRANCH_ADMIN".equals(type)) {
			Long adminId = (Long) request.getAttribute("userId");
			Admin admin = adminMapper.selectById(adminId);
			if (admin != null) {
				adminBranchId = admin.getBranchId();
			}
		}
		
		List<Map<String, Object>> out = new ArrayList<>();
        // 积极分子
        for (PositiveMemberApplication a : safeList(positiveMapper.selectAll())) {
			// 支部管理员只能看到所属支部的申请
			if (adminBranchId != null) {
				com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
				if (mb == null || !adminBranchId.equals(mb.getBranchId())) {
					continue;
				}
			}
			
			Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
			m.put("type", "积极分子申请");
            com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
            if (mb != null) {
                m.put("name", mb.getUsername());
                m.put("studentId", mb.getStudentId());
            }
			m.put("status", mapStatus(a.getStatus()));
            // 提交时间来自流程表
            ApplicationProcess pr = processMapper.selectByUserAndStage(a.getUserId(), "POSITIVE_MEMBER");
            m.put("submittedAt", pr == null ? null : pr.getSubmittedAt());
            m.put("comments", a.getComments());
            // 审核人与时间（如已审核）
            if (a.getReviewedAt() != null && a.getReviewerId() != null) {
                Admin r = adminMapper.selectById(a.getReviewerId());
                m.put("reviewer", r == null ? ("#" + a.getReviewerId()) : (r.getRealName() == null ? r.getUsername() : r.getRealName()));
                m.put("reviewTime", a.getReviewedAt());
            }
			out.add(m);
		}
		// 入党申请
        for (PartyApplication a : safeList(partyMapper.selectAll())) {
			// 支部管理员只能看到所属支部的申请
			if (adminBranchId != null) {
				com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
				if (mb == null || !adminBranchId.equals(mb.getBranchId())) {
					continue;
				}
			}
			
			Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
			m.put("type", "入党申请");
            com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
            if (mb != null) {
                m.put("name", mb.getUsername());
                m.put("studentId", mb.getStudentId());
            }
			m.put("status", mapStatus(a.getStatus()));
            ApplicationProcess pr = processMapper.selectByUserAndStage(a.getUserId(), "PARTY_APPLICATION");
            m.put("submittedAt", pr == null ? null : pr.getSubmittedAt());
            m.put("comments", a.getComments());
            if (a.getReviewedAt() != null && a.getReviewerId() != null) {
                Admin r = adminMapper.selectById(a.getReviewerId());
                m.put("reviewer", r == null ? ("#" + a.getReviewerId()) : (r.getRealName() == null ? r.getUsername() : r.getRealName()));
                m.put("reviewTime", a.getReviewedAt());
            }
			out.add(m);
		}
		// 预备党员
        for (PrepareMemberApplication a : safeList(prepareMapper.selectAll())) {
			// 支部管理员只能看到所属支部的申请
			if (adminBranchId != null) {
				com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
				if (mb == null || !adminBranchId.equals(mb.getBranchId())) {
					continue;
				}
			}
			
			Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
			m.put("type", "预备党员申请");
            com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
            if (mb != null) {
                m.put("name", mb.getUsername());
                m.put("studentId", mb.getStudentId());
            }
			m.put("status", mapStatus(a.getStatus()));
            ApplicationProcess pr = processMapper.selectByUserAndStage(a.getUserId(), "PREPARE_MEMBER");
            m.put("submittedAt", pr == null ? null : pr.getSubmittedAt());
            m.put("comments", a.getComments());
            if (a.getReviewedAt() != null && a.getReviewerId() != null) {
                Admin r = adminMapper.selectById(a.getReviewerId());
                m.put("reviewer", r == null ? ("#" + a.getReviewerId()) : (r.getRealName() == null ? r.getUsername() : r.getRealName()));
                m.put("reviewTime", a.getReviewedAt());
            }
			out.add(m);
		}
        // 简单按提交时间倒序
        out.sort((a,b) -> String.valueOf(b.get("submittedAt")).compareTo(String.valueOf(a.get("submittedAt"))));
		return Result.success(out);
	}

	@GetMapping("/admin/applications/{type}/{id}")
	public Result<Map<String, Object>> getDetail(HttpServletRequest request,
			@PathVariable("type") String type,
			@PathVariable("id") Long id) {
		Object userType = request.getAttribute("userType");
		if (userType == null) return Result.error(401, "未登录");
		String t = userType.toString();
		if (!"SYSTEM_ADMIN".equals(t) && !"BRANCH_ADMIN".equals(t)) return Result.error(403, "无权限");
		Map<String, Object> m = new HashMap<>();
		if ("positive".equalsIgnoreCase(type)) {
			PositiveMemberApplication a = positiveMapper.selectById(id);
			if (a == null) return Result.error(404, "不存在");
			m.put("id", a.getId());
			m.put("type", "积极分子申请");
			m.put("details", a.getApplicationDetails());
			m.put("attachments", a.getSupportingDocuments());
			m.put("status", mapStatus(a.getStatus()));
			m.put("comments", a.getComments());
			return Result.success(m);
		}
        if ("party".equalsIgnoreCase(type)) {
			PartyApplication a = partyMapper.selectById(id);
			if (a == null) return Result.error(404, "不存在");
			m.put("id", a.getId());
			m.put("type", "入党申请");
			m.put("details", a.getApplicationDetails());
            m.put("trainingRecords", a.getTrainingRecords());
            // 训练记录字段复用为附件，若为JSON数组则透出attachments
            List<String> att = parseStringArray(a.getTrainingRecords());
            if (att != null && !att.isEmpty()) {
                m.put("attachments", att);
            }
			m.put("status", mapStatus(a.getStatus()));
			m.put("comments", a.getComments());
			return Result.success(m);
		}
		if ("prepare".equalsIgnoreCase(type)) {
			PrepareMemberApplication a = prepareMapper.selectById(id);
			if (a == null) return Result.error(404, "不存在");
			m.put("id", a.getId());
			m.put("type", "预备党员申请");
            // 支持 evaluation_report 为JSON {text, attachments}
            String er = a.getEvaluationReport();
            String text = er;
            try {
                if (er != null && er.trim().startsWith("{")) {
                    JsonNode node = objectMapper.readTree(er);
                    if (node != null) {
                    JsonNode textNode = node.get("text");
                    if (textNode != null && !textNode.isNull()) text = textNode.asText();
                        JsonNode arr = node.get("attachments");
                        if (arr != null && arr.isArray()) {
                            List<String> att2 = objectMapper.convertValue(arr, new TypeReference<List<String>>(){});
                            if (att2 != null && !att2.isEmpty()) m.put("attachments", att2);
                        }
                    }
                }
            } catch (Exception ignore) {}
            m.put("evaluationReport", text);
			m.put("probationStart", a.getProbationPeriodStart());
			m.put("probationEnd", a.getProbationPeriodEnd());
			m.put("status", mapStatus(a.getStatus()));
			m.put("comments", a.getComments());
			return Result.success(m);
		}
		return Result.error(400, "未知类型");
	}

	/**
	 * 审核：支部管理员先审（记录 comments），超级管理员终审通过才算通过
	 */
    @PostMapping("/admin/applications/review/{type}/{id}")
    public Result<String> review(HttpServletRequest request,
			@PathVariable("type") String type,
			@PathVariable("id") Long id,
			@RequestParam("approve") boolean approve,
			@RequestParam(value = "comments", required = false) String comments) {
		Object userType = request.getAttribute("userType");
		Long adminId = (Long) request.getAttribute("userId");
		if (userType == null) return Result.error(401, "未登录");
        String t = userType.toString();
        // 支持 BRANCH_ADMIN 和 SYSTEM_ADMIN
        if (!"SYSTEM_ADMIN".equals(t) && !"BRANCH_ADMIN".equals(t)) return Result.error(403, "无权限");
        
        // 获取当前管理员的支部ID（如果是支部管理员）
        Long adminBranchId = null;
        if ("BRANCH_ADMIN".equals(t)) {
            Admin admin = adminMapper.selectById(adminId);
            if (admin != null) {
                adminBranchId = admin.getBranchId();
            }
        }

		if ("positive".equalsIgnoreCase(type)) {
			PositiveMemberApplication a = positiveMapper.selectById(id);
			if (a == null) return Result.error(404, "不存在");
			
			// 支部管理员只能审核所属支部的申请
			if (adminBranchId != null) {
				com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
				if (mb == null || !adminBranchId.equals(mb.getBranchId())) {
					return Result.error(403, "只能审核所属支部的申请");
				}
			}
			
            a.setComments(comments);
            a.setReviewerId(adminId);
            a.setReviewedAt(LocalDateTime.now());
            a.setStatus(approve ? "APPROVED" : "REJECTED");
            positiveMapper.update(a);
            return Result.success("审核完成");
		}
		if ("party".equalsIgnoreCase(type)) {
			PartyApplication a = partyMapper.selectById(id);
			if (a == null) return Result.error(404, "不存在");
			
			// 支部管理员只能审核所属支部的申请
			if (adminBranchId != null) {
				com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
				if (mb == null || !adminBranchId.equals(mb.getBranchId())) {
					return Result.error(403, "只能审核所属支部的申请");
				}
			}
			
            a.setComments(comments);
            a.setReviewerId(adminId);
            a.setReviewedAt(LocalDateTime.now());
            a.setStatus(approve ? "APPROVED" : "REJECTED");
            partyMapper.update(a);
            return Result.success("审核完成");
		}
		if ("prepare".equalsIgnoreCase(type)) {
			PrepareMemberApplication a = prepareMapper.selectById(id);
			if (a == null) return Result.error(404, "不存在");
			
			// 支部管理员只能审核所属支部的申请
			if (adminBranchId != null) {
				com.example.pojo.entity.Member mb = memberMapper.selectById(a.getUserId());
				if (mb == null || !adminBranchId.equals(mb.getBranchId())) {
					return Result.error(403, "只能审核所属支部的申请");
				}
			}
			
            a.setComments(comments);
            a.setReviewerId(adminId);
            a.setReviewedAt(LocalDateTime.now());
            a.setStatus(approve ? "APPROVED" : "REJECTED");
            prepareMapper.update(a);
            return Result.success("审核完成");
		}
		return Result.error(400, "未知类型");
	}

	private void upsertProcess(Long userId, String stage) {
		ApplicationProcess p = processMapper.selectByUserAndStage(userId, stage);
        if (p == null) {
			p = new ApplicationProcess();
			p.setUserId(userId);
			p.setCurrentStage(stage);
			p.setStatus("PENDING");
            p.setReviewerId(getDefaultReviewerId());
			p.setSubmittedAt(LocalDateTime.now());
			processMapper.insert(p);
		} else {
			p.setStatus("PENDING");
			p.setReviewedAt(null);
			processMapper.update(p);
		}
	}

	private <T> List<T> safeList(List<T> in) {
		return in == null ? java.util.Collections.emptyList() : in;
	}

	private String mapStatus(String code) {
		if (code == null || code.isEmpty()) return "待审核";
		if ("PENDING".equals(code)) return "待审核";
		if ("APPROVED".equals(code)) return "已通过";
		if ("REJECTED".equals(code)) return "已拒绝";
		return code;
	}

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    private List<String> parseStringArray(String json) {
        if (json == null || json.trim().isEmpty()) return java.util.Collections.emptyList();
        try {
            if (json.trim().startsWith("[")) {
                return objectMapper.readValue(json, new TypeReference<List<String>>(){});
            }
        } catch (Exception ignore) {}
        return java.util.Collections.emptyList();
    }

    /**
     * 获取默认的审核人（支部管理员）ID，避免外键约束失败。
     * 策略：优先取 id=1 的管理员；若不存在，则回退为系统管理员最小ID；再无则返回 null（由数据库允许 null）。
     */
    private Long getDefaultReviewerId() {
        try {
            // 优先系统管理员
            java.util.List<com.example.pojo.entity.Admin> sys = adminMapper.selectByAdminType("SYSTEM_ADMIN");
            if (sys != null && !sys.isEmpty()) return sys.get(0).getId();
            // 其次支部管理员
            java.util.List<com.example.pojo.entity.Admin> br = adminMapper.selectByAdminType("BRANCH_ADMIN");
            if (br != null && !br.isEmpty()) return br.get(0).getId();
            // 兜底取 1 号或 2 号
            com.example.pojo.entity.Admin a = adminMapper.selectById(1L);
            if (a != null) return a.getId();
        } catch (Exception ignore) {}
        try {
            // 尝试查询最小ID（若存在相应方法）
            com.example.pojo.entity.Admin b = adminMapper.selectById(2L);
            if (b != null) return b.getId();
        } catch (Exception ignore) {}
        return null;
    }
}


