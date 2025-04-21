// package project.OOAD_PROJECT.service;

// import com.stripe.exception.StripeException;
// import com.stripe.model.checkout.Session;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import project.OOAD_PROJECT.model.CheckoutRequest;
// import project.OOAD_PROJECT.model.PaidCourse;
// import project.OOAD_PROJECT.repository.PaidCourseRepository;
// import project.OOAD_PROJECT.service.StripeService;

// @Service
// public class PaymentService {

//     @Autowired
//     private StripeService stripeService;

//     @Autowired
//     private PaidCourseRepository paidCourseRepository;

//     public Session createCheckoutSession(Long courseId, Long userId, CheckoutRequest request) throws StripeException {
//         PaidCourse course = paidCourseRepository.findById(courseId)
//                 .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

//         // Pass course + user info to Stripe as metadata
//         return stripeService.createCheckoutSession(
//                 request.getAmount(),
//                 request.getCurrency(),
//                 request.getSuccessUrl(),
//                 request.getCancelUrl(),
//                 courseId,
//                 userId
//         );
//     }
// }


//new

package project.OOAD_PROJECT.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.OOAD_PROJECT.model.CheckoutRequest;
import project.OOAD_PROJECT.repository.PaidCourseRepository;
import project.OOAD_PROJECT.repository.UserRepository;
import project.OOAD_PROJECT.service.StripeService;
import project.OOAD_PROJECT.model.PaidCourse;

@Service
public class PaymentService {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private PaidCourseRepository paidCourseRepository;

    @Autowired
    private UserRepository userRepository;

    public Session createCheckoutSession(Long userId, Long courseId, CheckoutRequest request) throws StripeException {
        // Find the course and user from the database
        PaidCourse course = paidCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        // Retrieve the user, possibly for validation or metadata
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Pass course + user info to Stripe as metadata
        return stripeService.createCheckoutSession(
                request.getAmount(),
                request.getCurrency(),
                request.getSuccessUrl(),
                request.getCancelUrl(),
                courseId,
                userId
        );
    }
}
