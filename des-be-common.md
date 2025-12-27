# MÔ TẢ CHI TIẾT DỰ ÁN BE-COMMON

## 1. TỔNG QUAN DỰ ÁN

### 1.1. Thông tin cơ bản
- **Tên dự án**: be-common
- **Phiên bản**: 0.0.2
- **Mục đích**: Thư viện chung (Common Library) cho các microservices Spring Boot
- **Loại dự án**: Maven JAR Library
- **Ngôn ngữ**: Java 17
- **Framework**: Spring Boot 3.2.3, Spring Cloud 2023.0.4
- **Tác giả**: Nguyen The Anh
- **Repository**: https://github.com/NguuyenTheAnh/be-common
- **License**: Apache License 2.0

### 1.2. Mô tả chức năng
be-common là một thư viện Java được thiết kế để cung cấp các tiện ích và cấu trúc nền tảng chung cho các dự án Spring Boot microservices, bao gồm:
- Các lớp Base Entity và DTO với hỗ trợ auditing
- Service và Repository trừu tượng để giảm thiểu code CRUD lặp lại
- Quản lý Soft Delete (xóa mềm) tự động
- Xử lý exception toàn cục với hỗ trợ đa ngôn ngữ (i18n)
- Chuẩn hóa response API
- Tích hợp Spring Security cho auditing
- Hỗ trợ đa ngôn ngữ (Tiếng Việt và Tiếng Anh)

---

## 2. CẤU TRÚC DỰ ÁN

### 2.1. Cấu trúc thư mục
```
be-common/
├── src/main/
│   ├── java/com/theanh/common/
│   │   ├── auditing/          # Cấu hình auditing
│   │   ├── base/              # Các lớp base trừu tượng
│   │   ├── common/            # Cấu hình chung
│   │   ├── config/            # Các configuration class
│   │   ├── constant/          # Các hằng số
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── exception/         # Xử lý exception
│   │   ├── security/          # Security utilities
│   │   └── util/              # Các utility class
│   └── resources/
│       └── messages/          # File đa ngôn ngữ
├── target/                    # Compiled files
└── pom.xml                    # Maven configuration
```

---

## 3. CHI TIẾT CÁC PACKAGE VÀ FILE

### 3.1. Package: `auditing`

#### **AuditorAwareImpl.java**
- **Mục đích**: Triển khai Spring Data JPA AuditorAware để tự động ghi nhận user thực hiện thao tác
- **Chức năng**: 
  - Lấy username hiện tại từ CurrentUserProvider
  - Tự động điền thông tin `createdUser` và `updatedUser` vào entity
- **Sử dụng**: Tự động được kích hoạt bởi `@EnableJpaAuditing`

```java
// Lấy thông tin user hiện tại từ Security Context
public Optional<String> getCurrentAuditor() {
    return currentUserProvider.getCurrentUsername();
}
```

---

### 3.2. Package: `base`

#### **BaseEntity.java**
- **Mục đích**: Lớp trừu tượng cho tất cả các Entity trong hệ thống
- **Thuộc tính**:
  - `id` (Long): Primary key tự tăng
  - `isActive` (Boolean): Trạng thái kích hoạt (mặc định: true)
  - `isDeleted` (Boolean): Đánh dấu đã xóa mềm (mặc định: false)
  - `createdUser` (String): User tạo bản ghi
  - `updatedUser` (String): User cập nhật bản ghi
  - `createdDate` (LocalDateTime): Ngày tạo (tự động)
  - `updatedDate` (LocalDateTime): Ngày cập nhật (tự động)
- **Annotation**: 
  - `@MappedSuperclass`: Cho phép các entity con kế thừa các thuộc tính
  - `@EntityListeners(AuditingEntityListener.class)`: Tự động auditing

#### **BaseDto.java**
- **Mục đích**: Lớp trừu tượng cho tất cả các DTO
- **Thuộc tính**: 
  - `id` (Long): ID của đối tượng
- **Sử dụng**: Extend class này cho các DTO để có sẵn ID field

#### **BaseRepository.java**
- **Mục đích**: Interface repository cơ bản
- **Kế thừa**: 
  - `JpaRepository<T, ID>`: CRUD operations
  - `JpaSpecificationExecutor<T>`: Query với Specification
- **Annotation**: `@NoRepositoryBean` - Đánh dấu không tạo bean cho interface này
- **Sử dụng**: Extend interface này cho các repository cụ thể

#### **BaseService.java**
- **Mục đích**: Interface định nghĩa các phương thức service cơ bản
- **Phương thức**:
  - `saveObject(D dto)`: Lưu một đối tượng
  - `saveListObject(List<D> dtoList)`: Lưu danh sách đối tượng
  - `findById(ID id)`: Tìm theo ID (loại bỏ bản ghi đã xóa)
  - `findByIds(List<ID> ids)`: Tìm theo danh sách ID
  - `findAll()`: Lấy tất cả (loại bỏ bản ghi đã xóa)
  - `deleteById(ID id)`: Xóa mềm (set isDeleted = true)
  - `deleteByIds(List<ID> ids)`: Xóa mềm nhiều bản ghi
  - `deleteDataById(ID id)`: Xóa vật lý khỏi database
  - `deleteDataByIds(List<ID> ids)`: Xóa vật lý nhiều bản ghi

#### **BaseServiceImpl.java**
- **Mục đích**: Triển khai các phương thức service cơ bản
- **Tính năng nổi bật**:
  - **Soft Delete**: Tự động loại bỏ các bản ghi có `isDeleted = true` khỏi kết quả query
  - **Auto Mapping**: Sử dụng ModelMapper để chuyển đổi giữa Entity và DTO
  - **Transaction**: Tất cả các phương thức đều chạy trong transaction
  - **Specification Pattern**: Sử dụng JPA Specification để build query động
- **Phương thức hỗ trợ**:
  - `notDeletedSpec()`: Specification để lọc bản ghi chưa xóa
  - `idEqualsSpec(ID id)`: Specification để tìm theo ID
- **Sử dụng**: 
  - Extend class này trong service cụ thể
  - Override `getEntityClass()` và `getDtoClass()`

---

### 3.3. Package: `common`

#### **MapperConfig.java**
- **Mục đích**: Cấu hình ModelMapper bean
- **Cấu hình**: 
  - `MatchingStrategies.STRICT`: Mapping chính xác giữa các field
- **Sử dụng**: Tự động được Spring Container quản lý

#### **MessageConfig.java**
- **Mục đích**: Cấu hình MessageSource cho i18n
- **Cấu hình**:
  - Basename: `classpath:messages/messages`
  - Encoding: UTF-8
  - Cache: 10 giây
- **Chức năng**: Load các file message properties theo locale

---

### 3.4. Package: `config`

#### **AuditingConfig.java**
- **Mục đích**: Cấu hình JPA Auditing
- **Bean**:
  - `auditorAware()`: Tạo AuditorAware bean để cung cấp thông tin user
  - `securityContextCurrentUserProvider()`: Tạo default CurrentUserProvider nếu chưa có bean nào
- **Annotation**: `@EnableJpaAuditing(auditorAwareRef = "auditorAware")`
- **Lưu ý**: Developer có thể override CurrentUserProvider bean trong project riêng

---

### 3.5. Package: `constant`

#### **MessageConstants.java**
- **Mục đích**: Định nghĩa các message key constants
- **Các constants**:
  - `SYSTEM_ERROR`: Lỗi hệ thống
  - `ACCESS_DENIED`: Từ chối truy cập
  - `VALIDATE_REQUEST`: Lỗi validation
  - `DATA_NOT_FOUND`: Không tìm thấy dữ liệu
  - `DATA_EXISTS`: Dữ liệu đã tồn tại
  - `DATA_FAIL`: Xử lý dữ liệu thất bại
  - `OPERATION_SUCCESS`: Thao tác thành công
  - `OPERATION_FAILED`: Thao tác thất bại
  - `CONSTRAINT_ERROR`: Lỗi ràng buộc database
  - `NULL_POINT_EXCEPTION`: Null pointer
  - `MINIO_ERROR`: Lỗi MinIO

---

### 3.6. Package: `dto`

#### **ResponseDto.java**
- **Mục đích**: DTO chuẩn hóa cho tất cả API response
- **Thuộc tính**:
  - `code` (String): Mã trạng thái ("00" = success)
  - `message` (String): Thông báo
  - `data` (T): Dữ liệu response (generic type)
- **Annotation**: 
  - `@Data`: Auto generate getters/setters
  - `@Builder`: Hỗ trợ Builder pattern
- **Sử dụng**: Tất cả API response đều trả về object này

---

### 3.7. Package: `exception`

#### **BusinessException.java**
- **Mục đích**: Exception cho các lỗi nghiệp vụ
- **Thuộc tính**:
  - `code` (String): Mã lỗi
  - `errorData` (Serializable): Dữ liệu lỗi chi tiết
- **Constructor**:
  - Hỗ trợ nhiều constructor với các tham số khác nhau
  - Tự động log error khi khởi tạo
- **Sử dụng**: Throw exception này khi có lỗi nghiệp vụ

```java
throw new BusinessException(MessageConstants.DATA_NOT_FOUND);
```

#### **ExceptionHandlingController.java**
- **Mục đích**: Global exception handler cho toàn bộ application
- **Annotation**: `@RestControllerAdvice` - Áp dụng cho tất cả controllers
- **Xử lý các exception**:
  1. **BusinessException**: Lỗi nghiệp vụ
     - Status: 400 BAD_REQUEST
     - Tự động lấy message từ message bundle theo code
  
  2. **MethodArgumentNotValidException**: Lỗi validation (@Valid)
     - Status: 400 BAD_REQUEST
     - Trả về map các field và message lỗi
  
  3. **AccessDeniedException**: Từ chối quyền truy cập
     - Status: 403 FORBIDDEN
  
  4. **DataIntegrityViolationException**: Lỗi ràng buộc database
     - Status: 406 NOT_ACCEPTABLE
     - Xử lý ConstraintViolationException
  
  5. **Exception/RuntimeException**: Lỗi hệ thống chưa xử lý
     - Status: 500 INTERNAL_SERVER_ERROR
     - Log chi tiết error stack trace

---

### 3.8. Package: `security`

#### **CurrentUserProvider.java**
- **Mục đích**: Interface để lấy thông tin user hiện tại
- **Phương thức**: `getCurrentUsername()` - Trả về Optional<String>
- **Sử dụng**: Implement interface này để custom cách lấy user

#### **SecurityContextCurrentUserProvider.java**
- **Mục đích**: Implementation mặc định của CurrentUserProvider
- **Chức năng**: 
  - Lấy user từ Spring SecurityContext
  - Hỗ trợ cả UserDetails và String principal
  - Bỏ qua "anonymousUser"
- **Điều kiện**: Chỉ được tạo bean khi chưa có CurrentUserProvider nào
- **Override**: Developer có thể tạo bean CurrentUserProvider riêng để thay thế

---

### 3.9. Package: `util`

#### **DataUtil.java**
- **Mục đích**: Utility class cho xử lý dữ liệu
- **Phương thức**:
  1. `convertObject(S source, Function<S,T> mapper)`: 
     - Convert một object sang type khác
     - Null-safe
  
  2. `convertList(List<S> sourceList, Function<S,T> mapper)`:
     - Convert list object sang list type khác
     - Trả về empty list nếu input null
  
  3. `isNullOrEmpty(List<?> list)`:
     - Check list null hoặc empty
- **Sử dụng**: Các utility methods static

#### **MessageCommon.java**
- **Mục đích**: Utility để lấy message từ message bundle
- **Dependencies**: MessageSource (auto-inject)
- **Phương thức**:
  1. `getValueByMessageCode(String code)`: Lấy message theo code
  2. `getValueByMessageCode(String code, Object[] args)`: Lấy message với parameters
  3. `getMessage(String code, Object... params)`: Lấy message, fallback về code nếu không tìm thấy
- **Locale**: Tự động lấy locale từ LocaleContextHolder
- **Sử dụng**: Inject MessageCommon bean và gọi các phương thức

#### **ResponseConfig.java**
- **Mục đích**: Utility để tạo standardized API response
- **Constant**: `SUCCESS_CODE = "00"`
- **Phương thức**:

  **Success Response:**
  1. `success(T body)`: Response thành công với data
  2. `success(T body, String message)`: Response thành công với data và message
  
  **Error Response:**
  3. `error(HttpStatus status, String errorCode, String message)`: Error response cơ bản
  4. `error(HttpStatus status, T data, String code)`: Error response với data
  
  **File Download:**
  5. `downloadFile(String fileName, InputStreamResource input)`: Download file
  6. `downloadFile(String fileName, ByteArrayInputStream stream)`: Download từ ByteArray

- **Sử dụng trong Controller**:
```java
return ResponseConfig.success(userData, "Lấy dữ liệu thành công");
return ResponseConfig.error(BAD_REQUEST, "ERR001", "Dữ liệu không hợp lệ");
```

---

### 3.10. Package: `resources/messages`

#### **application.yml**
- **Mục đích**: Cấu hình Spring messages
- **Cấu hình**:
  - basename: messages
  - encoding: UTF-8

#### **messages.properties** (Tiếng Việt - Mặc định)
- **Mục đích**: File message mặc định (Tiếng Việt)
- **Nội dung**: 
  - Lỗi hệ thống: "Lỗi hệ thống. Vui lòng thử lại sau."
  - Từ chối truy cập: "Bạn không có quyền truy cập tài nguyên này."
  - Validation: "Dữ liệu gửi lên không hợp lệ."
  - Không tìm thấy: "Không tìm thấy dữ liệu."
  - Đã tồn tại: "Dữ liệu đã tồn tại."
  - Và nhiều message khác...

#### **messages_vi.properties** (Tiếng Việt)
- **Mục đích**: File message tiếng Việt (giống messages.properties)
- **Locale**: vi_VN

#### **messages_en.properties** (Tiếng Anh)
- **Mục đích**: File message tiếng Anh
- **Nội dung**:
  - System error: "System error. Please try again later."
  - Access denied: "You are not authorized to access this resource."
  - Validation: "Invalid request payload."
  - Not found: "Data not found."
  - Already exists: "Data already exists."
  - Và các message tương ứng...
- **Locale**: en_US

---

## 4. DEPENDENCIES (pom.xml)

### 4.1. Core Dependencies
- **Spring Boot Starter** (3.2.3): Core Spring Boot
- **Spring Data JPA**: ORM và database access
- **Spring Validation**: Bean validation
- **Spring Security**: Authentication và authorization core
- **Spring Cloud OpenFeign**: HTTP client cho microservices

### 4.2. Utility Libraries
- **ModelMapper** (3.1.1): Object mapping (Entity ↔ DTO)
- **Gson**: JSON processing
- **Guava** (33.1.0-jre): Google common utilities
- **Java JWT** (4.4.0): JWT token processing

### 4.3. Development Tools
- **Lombok**: Reduce boilerplate code (@Data, @Builder, @Getter, @Setter...)

---

## 5. CÁCH SỬ DỤNG TRONG DỰ ÁN

### 5.1. Thêm dependency vào project

```xml
<dependency>
  <groupId>com.theanh</groupId>
  <artifactId>be-common</artifactId>
  <version>0.0.2</version>
</dependency>
```

### 5.2. Đảm bảo component scan

Trong application main class:
```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.your.package",
    "com.theanh.common"  // Scan be-common package
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 5.3. Tạo Entity kế thừa BaseEntity

```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String username;
    private String email;
    private String password;
    // ... other fields
}
```

### 5.4. Tạo DTO kế thừa BaseDto

```java
public class UserDto extends BaseDto {
    private String username;
    private String email;
    // ... other fields (không include password)
}
```

### 5.5. Tạo Repository kế thừa BaseRepository

```java
@Repository
public interface UserRepository extends BaseRepository<User, Long> {
    // Custom query methods nếu cần
    Optional<User> findByUsername(String username);
}
```

### 5.6. Tạo Service kế thừa BaseServiceImpl

```java
@Service
public class UserService extends BaseServiceImpl<User, UserDto, Long> {
    
    public UserService(UserRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }
    
    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }
    
    @Override
    protected Class<UserDto> getDtoClass() {
        return UserDto.class;
    }
    
    // Custom methods
    public UserDto findByUsername(String username) {
        User user = ((UserRepository) repository)
            .findByUsername(username)
            .orElseThrow(() -> new BusinessException(MessageConstants.DATA_NOT_FOUND));
        return modelMapper.map(user, UserDto.class);
    }
}
```

### 5.7. Sử dụng trong Controller

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        return ResponseConfig.success(user, "Lấy thông tin thành công");
    }
    
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto dto) {
        UserDto saved = userService.saveObject(dto);
        return ResponseConfig.success(saved, "Tạo user thành công");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteById(id); // Soft delete
        return ResponseConfig.success(null, "Xóa user thành công");
    }
}
```

### 5.8. Throw BusinessException khi có lỗi

```java
if (existingUser != null) {
    throw new BusinessException(MessageConstants.DATA_EXISTS, "Username đã tồn tại");
}
```

### 5.9. Override CurrentUserProvider (Optional)

Nếu muốn custom cách lấy user:
```java
@Component
public class CustomUserProvider implements CurrentUserProvider {
    @Override
    public Optional<String> getCurrentUsername() {
        // Custom logic để lấy username
        // Ví dụ: từ JWT token, từ HTTP header, etc.
        return Optional.of("custom_user");
    }
}
```

---

## 6. TÍNH NĂNG NỔI BẬT

### 6.1. Soft Delete (Xóa mềm)
- Tự động set `isDeleted = true` thay vì xóa khỏi database
- Tất cả query đều tự động loại bỏ bản ghi đã xóa
- Có thể hard delete nếu cần: `deleteDataById()`

### 6.2. Auto Auditing
- Tự động ghi nhận `createdDate`, `updatedDate`
- Tự động ghi nhận `createdUser`, `updatedUser` từ SecurityContext
- Không cần code thủ công

### 6.3. Global Exception Handling
- Tự động bắt và xử lý tất cả exception
- Trả về response chuẩn hóa
- Hỗ trợ đa ngôn ngữ cho error message
- Log chi tiết cho debugging

### 6.4. Standardized Response
- Tất cả API đều trả về cùng format: `{code, message, data}`
- Success code: "00"
- Error code: custom theo nghiệp vụ
- Dễ dàng parse ở client side

### 6.5. Internationalization (i18n)
- Hỗ trợ đa ngôn ngữ (Vietnamese, English)
- Tự động chọn locale theo HTTP header `Accept-Language`
- Dễ dàng thêm ngôn ngữ mới bằng cách thêm file properties

### 6.6. Reduce Boilerplate Code
- Không cần viết lại CRUD methods
- Không cần viết lại soft delete logic
- Không cần viết lại exception handling
- Tiết kiệm 60-70% code trong mỗi service

---

## 7. BEST PRACTICES

### 7.1. Khi nào nên sử dụng be-common?
✅ Dự án microservices với nhiều service cần chuẩn hóa
✅ Cần soft delete và auditing
✅ Cần standardized API response
✅ Cần hỗ trợ đa ngôn ngữ
✅ Muốn giảm boilerplate code

### 7.2. Khi nào KHÔNG nên sử dụng?
❌ Dự án nhỏ, đơn giản, không cần chuẩn hóa
❌ Cần custom logic phức tạp khác với base implementation
❌ Performance critical (vì có overhead của abstraction layer)

### 7.3. Tips sử dụng hiệu quả
1. **Override methods khi cần**: Không ngại override các methods trong BaseServiceImpl nếu cần logic đặc biệt
2. **Custom Specification**: Tận dụng JPA Specification để build complex queries
3. **Extend message bundle**: Thêm custom messages trong project riêng
4. **Logging**: Sử dụng BusinessException để log lỗi nghiệp vụ
5. **Transaction**: Tận dụng `@Transactional` đã có sẵn trong BaseServiceImpl

---

## 8. DEPLOYMENT & DISTRIBUTION

### 8.1. Build JAR
```bash
mvn clean package
```
Output: `target/be-common-0.0.2.jar`

### 8.2. Install to Local Maven Repository
```bash
mvn clean install
```

### 8.3. Deploy to GitHub Packages
```bash
mvn deploy
```
Cần cấu hình GitHub token trong `settings.xml`

### 8.4. Sử dụng từ GitHub Packages
Thêm vào `pom.xml` của project:
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/NguuyenTheAnh/be-common</url>
    </repository>
</repositories>
```

---

## 9. PHÁT TRIỂN TƯƠNG LAI

### 9.1. Các tính năng có thể thêm
- Caching support với Redis
- API versioning support
- Rate limiting utilities
- File upload/download helpers
- Email/SMS notification utilities
- Pagination và sorting utilities
- Search và filter helpers
- Async processing support
- Event publishing/subscribing
- Health check endpoints

### 9.2. Cải tiến
- Performance optimization
- More unit tests
- Better documentation
- More examples
- Integration with Spring Boot 3.x features

---

## 10. KẾT LUẬN

**be-common** là một thư viện mạnh mẽ và linh hoạt, giúp:
- ✅ Tăng tốc độ phát triển dự án
- ✅ Chuẩn hóa code và structure
- ✅ Giảm thiểu lỗi phổ biến
- ✅ Dễ dàng maintain và scale
- ✅ Tích hợp dễ dàng vào các dự án Spring Boot

Thư viện phù hợp cho các dự án microservices cần có sự nhất quán về cấu trúc code, xử lý exception, và API response format.

---

**Ngày tạo**: 27/12/2025  
**Phiên bản tài liệu**: 1.0  
**Tác giả tài liệu**: GitHub Copilot  
**Dự án**: be-common v0.0.2

