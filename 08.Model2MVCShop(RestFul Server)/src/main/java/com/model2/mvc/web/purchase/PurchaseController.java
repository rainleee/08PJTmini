package com.model2.mvc.web.purchase;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.purchase.PurchaseService;
import com.model2.mvc.service.user.UserService;


//==> 회원관리 Controller
@Controller
@RequestMapping("/purchase/*")
public class PurchaseController {
	
	///Field
	@Autowired
	@Qualifier("purchaseServiceImpl")
	private PurchaseService purchaseService;
	
	@Autowired
	@Qualifier("userServiceImpl")
	private UserService userService;
	
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	//setter Method 구현 않음
		
	public PurchaseController(){
		System.out.println(this.getClass());
	}
	
	//==> classpath:config/common.properties  ,  classpath:config/commonservice.xml 참조 할것
	//==> 아래의 두개를 주석을 풀어 의미를 확인 할것
	@Value("#{commonProperties['pageUnit']}")
	//@Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	//@Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;
	
	
//	@RequestMapping("/addPurchaseView.do")
//	public String addPurchaseView( @RequestParam("prodNo") int prodNo,
//									HttpSession session,
//									Model model) throws Exception {
	@RequestMapping(value = "addPurchase", method = RequestMethod.GET)
	public String addPurchase( @RequestParam("prodNo") int prodNo,
			HttpSession session,
			Model model) throws Exception {

		System.out.println("/purchase/addPurchase : GET");
		
		
		model.addAttribute("user",session.getAttribute("user"));
//		System.out.println("addPurchase user : " + session.getAttribute("user"));
		//product의 정보를 가져오기 위해서 선언
		Product product = productService.getProduct(prodNo);
		model.addAttribute("product",product);
		System.out.println("addPurchase product : " + product);
		
		return "forward:/purchase/addPurchaseView.jsp";
	}
	
//	@RequestMapping("/addPurchase.do")
//	public String addPurchase( @ModelAttribute("purchase") Purchase purchase,
//								@RequestParam("prodNo") int prodNo,
//								HttpSession session,
//									Model model) throws Exception {
	@RequestMapping(value = "addPurchase", method = RequestMethod.POST)
	public String addPurchase( @ModelAttribute("purchase") Purchase purchase,
			@RequestParam("prodNo") int prodNo,
			HttpSession session,
			Model model) throws Exception {

		System.out.println("/purchase/addPurchase : POST");
		//Business Logic
//		user의 정보를 가져오기 위해서 선언
		model.addAttribute("user",session.getAttribute("user"));
//		System.out.println("addPurchase user : " + session.getAttribute("user"));
		
		//product의 정보를 가져오기 위해서 선언
		Product product = productService.getProduct(prodNo);
		model.addAttribute("product",product);
//		System.out.println("addPurchase product : " + product);
		purchase.setBuyer((User)session.getAttribute("user"));
		purchase.setPurchaseProd(product);
		
		model.addAttribute("purchase",purchase);
		
		purchaseService.addPurchase(purchase);
		
		//담긴 정보를 넘기기위해서 redirect에서 forward로 변경
		return "forward:/purchase/addPurchase.jsp";
	}
	
//	@RequestMapping("/getPurchase.do")
//	public String getPurchase( @RequestParam("tranNo") int tranNo ,
//									Model model,
//									HttpSession session) throws Exception {
	@RequestMapping(value = "getPurchase", method = RequestMethod.GET)
	public String getPurchase( @RequestParam("tranNo") int tranNo ,
			Model model,
			HttpSession session) throws Exception {
		
		System.out.println("/pruchase/getPurchase :  GET");
		
		//Business Logic
		Purchase purchase = purchaseService.getPurchase(tranNo);
		System.out.println("purchase : " + purchase);
		
		// Model 과 View 연결
		model.addAttribute("purchase", purchase);
		
		session.setAttribute("purchase", purchase);
		
		return "forward:/purchase/getPurchase.jsp";
		
	}
	
	@RequestMapping(value = "updatePurchase", method = RequestMethod.GET)
	public String updatePurchase( @RequestParam("tranNo") int tranNo , Model model,
						HttpSession session) throws Exception{

		System.out.println("/purchase/updatePurchase : GET");
		//Business Logic
		Purchase purchase = purchaseService.getPurchase(tranNo);
		
		// Model 과 View 연결
		model.addAttribute("purchase", purchase);
		session.getAttribute("purchase");
		session.setAttribute("purchase", purchase);
		return "forward:/purchase/updatePurchaseView.jsp";
	}
	
	@RequestMapping(value = "updatePurchase", method = RequestMethod.POST)
	public String updatePurchase(@RequestParam("tranNo") int tranNo,
									@ModelAttribute("purchase") Purchase purchase,
									Model model) throws Exception{
		
		//먼저  view.jsp에 있는 업데이트 정보를 purchase에 넣어줌
		purchaseService.updatePurcahse(purchase);
		
		//기존에 있던 정보를 select
		Purchase purchaseSelect = purchaseService.getPurchase(tranNo);
		model.addAttribute("purchase",purchaseSelect);
		
		System.out.println("/purchase/updatePurchase : POST");
		
		
		return "forward:/purchase/updatePurchase.jsp";
		
	}
	
	@RequestMapping(value = "listPurchase")
	public String listPurchase( @ModelAttribute("search") Search search ,
								@ModelAttribute("user") User user,
								Model model, HttpSession session) throws Exception{
		
		System.out.println("/purchase/listPurchase GET / POST");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
//		System.out.println("search의 값(전) :" + search);
		search.setPageSize(pageSize);
		//아래 보여줄 pageUnit을 세팅해줬음. 페지이 모델어트리뷰트 추가
//		System.out.println("search의 값(후) :" + search);
		
		user = (User) session.getAttribute("user");
		
		
		
		// Business logic 수행
		Map<String , Object> map = purchaseService.getPurchaseList(search, user.getUserId());
		System.out.println("map : " + map);
		
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		System.out.println("model  : " + model);
		
		return "forward:/purchase/listPurchase.jsp";
	}
	
	@RequestMapping(value = "listPurchaseProduct")
	public String listPurchaseProduct( @ModelAttribute("search") Search search ,
								@ModelAttribute("user") User user,
								Model model,HttpSession session) throws Exception{
		
		System.out.println("/purchase/listPurchaseProduct GET / POST");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		
		search.setPageSize(pageSize);
		System.out.println("dfdfdfdsf : " + search);
		
		//아래 보여줄 pageUnit을 세팅해줬음. 페지이 모델어트리뷰트 추가
		
		user = (User) session.getAttribute("user");
		
		
		
		// Business logic 수행
		Map<String , Object> map = purchaseService.getListPurchaseProduct(search, user.getUserId());
		System.out.println("map : " + map);
		
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		
		return "forward:/purchase/listPurchaseProduct.jsp";
	}
	
	@RequestMapping(value = "listSale")
	public String listSale( @ModelAttribute("search") Search search ,
								Model model) throws Exception{
		
		System.out.println("/purchase/listSale GET / POST");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		
		search.setPageSize(pageSize);
		
		

		// Business logic 수행
		Map<String , Object> map = purchaseService.getSaleList(search);
		System.out.println("map : " + map);
		
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		System.out.println("model : " + model);
		
		return "forward:/purchase/listSale.jsp";
	}
	
	@RequestMapping(value = "updateTranCode", method =RequestMethod.GET)
	public String updateTranCode(@RequestParam("tranCode") String tranCode,
								@RequestParam("tranNo") int tranNo,
								@RequestParam("currentPage") int currentPage,
								@ModelAttribute("purchase") Purchase purchase,
								@ModelAttribute("search") Search search ,
								Model model ) throws Exception{
		
		
		System.out.println("/purchase/updateTranCode : GET");
		
		
		purchase.setTranNo(tranNo);
		System.out.println("dsdsd : " + currentPage);
		
		String resultForward = "/purchase/listSale?CurrentPage=" + currentPage;
//		String resultForward = "/listSale.do?CurrentPage=" + currentPage;
		
		
		if(purchase.getTranCode().trim().equals("2")) {
//			purchase.setTranCode(tranCode);
			search.setCurrentPage(currentPage);
					
			
		}else if(purchase.getTranCode().trim().equals("3")){
//			purchase.setTranCode(tranCode);
			search.setCurrentPage(currentPage);
			
//			resultForward = "/listPurchaseProduct.do?CurrentPage=" + currentPage;
			resultForward = "/purchase/listPurchaseProduct?CurrentPage=" + currentPage;
		}
		
//		System.out.println("purchase tranCode 비교(후) : " + purchase);
		
		// Business logic 수행
		purchaseService.updateTranCode(purchase);
		
		
		
		// Model 과 View 연결
		model.addAttribute("purchase", purchase);
		System.out.println("update purchase  : " + purchase);
		
		return resultForward;
	}
	
	
	
	
}