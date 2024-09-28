package org.cpts422.Femininomenon.App.Controllers;

import org.cpts422.Femininomenon.App.Models.InboxMessageModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.InboxMessageService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/user/inbox")
public class InboxMessageController {

    private final InboxMessageService inboxMessageService;
    private final UsersService usersService;

    @Autowired
    public InboxMessageController(InboxMessageService inboxMessageService, UsersService usersService) {
        this.inboxMessageService = inboxMessageService;
        this.usersService = usersService;
    }

    @GetMapping("/view")
    public String viewInbox(@RequestParam("userLogin") String userLogin, Model model) {
        UserModel user = usersService.findByLogin(userLogin);
        List<InboxMessageModel> messages = inboxMessageService.getInboxMessages(user);

        model.addAttribute("messages", messages);
        model.addAttribute("userLogin", userLogin);
        return "userInbox";
    }

}
