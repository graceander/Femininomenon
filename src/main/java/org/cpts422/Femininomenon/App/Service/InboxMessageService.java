package org.cpts422.Femininomenon.App.Service;

import org.cpts422.Femininomenon.App.Models.InboxMessageModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Repository.InboxMessageRepository;
import org.cpts422.Femininomenon.App.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InboxMessageService {

    private final InboxMessageRepository inboxMessageRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public InboxMessageService(InboxMessageRepository inboxMessageRepository, TransactionRepository transactionRepository) {
        this.inboxMessageRepository = inboxMessageRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<InboxMessageModel> getInboxMessages(UserModel user) {
        return inboxMessageRepository.findByUser(user);
    }

    public void addMessage(InboxMessageModel message) {
        inboxMessageRepository.save(message);
    }
    // check for overspending incomplete
    public void checkForOverspending(UserModel user, double limitAmount) {
        double totalSpending = 0.0;
        List<TransactionModel> transactions = transactionRepository.findByUserLogin(user.getLogin());


    }
}
