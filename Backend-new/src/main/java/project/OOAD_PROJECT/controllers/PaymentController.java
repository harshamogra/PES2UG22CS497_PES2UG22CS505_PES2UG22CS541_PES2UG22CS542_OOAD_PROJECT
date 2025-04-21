// package project.OOAD_PROJECT.controllers;

// import com.stripe.exception.StripeException;
// import com.stripe.model.checkout.Session;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import project.OOAD_PROJECT.model.CheckoutRequest;
// import project.OOAD_PROJECT.model.CheckoutResponse;
// import project.OOAD_PROJECT.model.PaidCourse;
// import project.OOAD_PROJECT.model.User;
// import project.OOAD_PROJECT.repository.PaidCourseRepository;
// import project.OOAD_PROJECT.repository.UserRepository;
// import project.OOAD_PROJECT.service.PaymentService;

// import java.util.Optional;

// @RestController
// @RequestMapping("/payments")
// public class PaymentController {

//     private final PaymentService paymentService;
//     private final UserRepository userRepository;
//     private final PaidCourseRepository courseRepository;

//     public PaymentController(PaymentService paymentService, UserRepository userRepository, PaidCourseRepository courseRepository) {
//         this.paymentService = paymentService;
//         this.userRepository = userRepository;
//         this.courseRepository = courseRepository;
//     }

//     @PostMapping("/checkout/{userId}/{courseId}")
//     public CheckoutResponse createCheckoutSession(
//             @PathVariable Long userId,
//             @PathVariable Long courseId,
//             @RequestBody CheckoutRequest checkoutRequest
//     ) {
//         try {
//             Session session = paymentService.createCheckoutSession(userId, courseId, checkoutRequest);

//             return new CheckoutResponse(
//                     checkoutRequest.getAmount(),
//                     checkoutRequest.getCurrency(),
//                     session.getUrl(),
//                     "created",
//                     session.getId()
//             );

//         } catch (StripeException e) {
//             throw new RuntimeException("Stripe error: " + e.getMessage());
//         } catch (IllegalArgumentException e) {
//             throw new RuntimeException("Error: " + e.getMessage());
//         }
//     }

//     @GetMapping("/success")
//     public ResponseEntity<String> handleSuccess(@RequestParam("session_id") String sessionId) {
//         try {
//             Session session = Session.retrieve(sessionId);

//             if ("complete".equals(session.getStatus()) || "paid".equals(session.getPaymentStatus())) {
//                 Long userId = Long.parseLong(session.getMetadata().get("userId"));
//                 Long courseId = Long.parseLong(session.getMetadata().get("courseId"));

//                 Optional<User> userOpt = userRepository.findById(userId);
//                 Optional<PaidCourse> courseOpt = courseRepository.findById(courseId);

//                 if (userOpt.isPresent() && courseOpt.isPresent()) {
//                     User user = userOpt.get();
//                     PaidCourse course = courseOpt.get();

//                     user.purchaseCourse(course);
//                     userRepository.save(user);

//                     return ResponseEntity.ok("✅ Course purchased and added successfully!");
//                 } else {
//                     return ResponseEntity.status(404).body("❌ User or Course not found.");
//                 }
//             } else {
//                 return ResponseEntity.badRequest().body("❌ Payment not successful.");
//             }

//         } catch (StripeException e) {
//             return ResponseEntity.status(500).body("❌ Error validating payment: " + e.getMessage());
//         }
//     }

//     @GetMapping("/failed")
//     public ResponseEntity<String> handleFailed() {
//         return ResponseEntity.ok("❌ Payment failed or was cancelled. Please try again.");
//     }
// }


// new
package project.OOAD_PROJECT.controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.OOAD_PROJECT.model.CheckoutRequest;
import project.OOAD_PROJECT.model.CheckoutResponse;
import project.OOAD_PROJECT.model.PaidCourse;
import project.OOAD_PROJECT.model.User;
import project.OOAD_PROJECT.repository.PaidCourseRepository;
import project.OOAD_PROJECT.repository.UserRepository;
import project.OOAD_PROJECT.service.PaymentService;

import java.util.Optional;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final PaidCourseRepository courseRepository;

    public PaymentController(PaymentService paymentService, UserRepository userRepository, PaidCourseRepository courseRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @PostMapping("/checkout/{userId}/{courseId}")
    public CheckoutResponse createCheckoutSession(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @RequestBody CheckoutRequest checkoutRequest
    ) {
        try {
            // Create the checkout session using the service
            Session session = paymentService.createCheckoutSession(userId, courseId, checkoutRequest);

            return new CheckoutResponse(
                    checkoutRequest.getAmount(),
                    checkoutRequest.getCurrency(),
                    session.getUrl(),
                    "created",
                    session.getId()
            );

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    public ResponseEntity<String> handleSuccess(@RequestParam("session_id") String sessionId) {
        try {
            // Retrieve the session to check its status
            Session session = Session.retrieve(sessionId);

            if ("complete".equals(session.getStatus()) || "paid".equals(session.getPaymentStatus())) {
                Long userId = Long.parseLong(session.getMetadata().get("userId"));
                Long courseId = Long.parseLong(session.getMetadata().get("courseId"));

                Optional<User> userOpt = userRepository.findById(userId);
                Optional<PaidCourse> courseOpt = courseRepository.findById(courseId);

                if (userOpt.isPresent() && courseOpt.isPresent()) {
                    User user = userOpt.get();
                    PaidCourse course = courseOpt.get();

                    // Associate the course with the user upon successful payment
                    user.purchaseCourse(course);
                    userRepository.save(user);

                    return ResponseEntity.ok("✅ Course purchased and added successfully!");
                } else {
                    return ResponseEntity.status(404).body("❌ User or Course not found.");
                }
            } else {
                return ResponseEntity.badRequest().body("❌ Payment not successful.");
            }

        } catch (StripeException e) {
            return ResponseEntity.status(500).body("❌ Error validating payment: " + e.getMessage());
        }
    }

    @GetMapping("/failed")
    public ResponseEntity<String> handleFailed() {
        return ResponseEntity.ok("❌ Payment failed or was cancelled. Please try again.");
    }
}
