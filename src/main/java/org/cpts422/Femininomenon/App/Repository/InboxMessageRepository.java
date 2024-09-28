package org.cpts422.Femininomenon.App.Repository;

import org.cpts422.Femininomenon.App.Models.InboxMessageModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboxMessageRepository extends JpaRepository<InboxMessageModel, Long> {
    List<InboxMessageModel> findByUser(UserModel user);
}
