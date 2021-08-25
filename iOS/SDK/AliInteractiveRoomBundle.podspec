Pod::Spec.new do |s|

	s.name = 'AliInteractiveRoomBundle'
	s.version = '1.2.1'
	s.summary = 'AliInteractiveRoomBundle framework.'	  
	s.description  = <<-DESC
	                   It's an SDK for interactive multimedia communication, which implement by Objective-C..
	                   DESC

	s.homepage = "http://gitlab.alibaba-inc.com/room-paas/room-paas-ios"
	s.license  = { :type => "MIT", :file => "LICENSE" }

	s.authors = { 'aliyun interactive video' => 'weihe.whq@alibaba-inc.com' }

	s.platform = :ios
	s.ios.deployment_target = '10.0'

	s.source = { :git => 'git@gitlab.alibaba-inc.com:room-paas/room-paas-ios.git', :tag => s.version.to_s }


	s.pod_target_xcconfig = {
       'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64'
    }

  
    s.user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }

    s.subspec 'AliInteractiveRoomBundle' do |inner_s|
	   inner_s.vendored_frameworks = 'AliInteractiveRoomBundle.framework'
	   inner_s.frameworks = 'Foundation', 'UIKit', 'SystemConfiguration', 'CoreTelephony'
    end

    s.requires_arc = true
   

end
