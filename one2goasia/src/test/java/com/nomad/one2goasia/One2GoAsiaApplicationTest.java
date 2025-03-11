// package com.nomad.one2goasia;

// import static org.mockito.Mockito.doNothing;

// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;

// import com.azure.messaging.servicebus.ServiceBusReceiverClient;
// import com.azure.messaging.servicebus.ServiceBusSenderClient;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.nomad.data_library.connectors.ServiceBusBatchSender;
// import com.nomad.data_library.messages.ScraperResponse;

// @SpringBootTest
// @Import(ServiceBusBatchMock.class)
// class One2GoAsiaApplicationTest {

    
//     @MockitoBean
//     ServiceBusSenderClient senderMock;
//     // @Autowired
//     // ServiceBusBatchSender<ScraperResponse> serviceBusBatchSender;

    

//     @MockitoBean
//     ServiceBusReceiverClient receiver;

// 	@Test
// 	void contextLoads() {
//         // Mockito.when(serviceBusBatchSender.getSenderClient()).thenReturn(senderMock);
//         // doNothing().when(senderMock);


// 	}

// }
