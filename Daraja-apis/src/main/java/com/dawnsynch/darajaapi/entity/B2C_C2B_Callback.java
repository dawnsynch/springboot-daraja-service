package com.dawnsynch.darajaapi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "b2c_c2b_callbacks")
public class B2C_C2B_Callback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer resultType;
    private Integer resultCode;
    private String resultDesc;
    private String originatorConversationId;
    private String conversationId;
    private String transactionId;

    private Double transactionAmount;
    private String transactionReceipt;
    private String receiverPartyPublicName;

    private LocalDateTime transactionCompletedDatetime;
    private Double b2cUtilityAccountFunds;
    private Double b2cWorkingAccountFunds;
    private String b2cRecipientRegisteredCustomer;
    private Double b2cChargesPaidAccountFunds;

    private String referenceItemKey;
    private String referenceItemValue;

    private LocalDateTime createdAt = LocalDateTime.now();

}
