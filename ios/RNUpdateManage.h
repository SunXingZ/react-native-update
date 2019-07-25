//
//  RNUpdateManage.h
//  RNUpdate
//
//  Created by 放心家 on 2019/7/24.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface RNUpdateManage : NSObject

+ (BOOL)resetJSBundlePath;

+ (BOOL)copyFileToPath:(NSString *)originPath targetPath:(NSString *)targetPath;

+ (BOOL)copyFileToURL:(NSURL *)originURL targetURL:(NSURL *)targetURL;

+ (BOOL)copyJSBundleFileToURL:(NSURL *)url;

+ (BOOL)copyAssetsFileToURL:(NSURL *)url;

+ (RCTRootView *)getRootViewModuleName:(NSString *)moduleName
                         launchOptions:(NSDictionary *)launchOptions;

+ (RCTBridge *)createBridgeWithBundleURL:(NSURL *)bundleURL;

+ (RCTRootView *)createRootViewWithURL:(NSURL *)url
                            moduleName:(NSString *)moduleName
                         launchOptions:(NSDictionary *)launchOptions;

+ (RCTRootView *)createRootViewWithModuleName:(NSString *)moduleName
                                       bridge:(RCTBridge *)bridge;

+ (NSURL *)URLForJSBundleInProject;

+ (NSURL *)URLForAssetsInProject;

+ (NSURL *)URLForJSBundleInDocumentsDirectory;

+ (NSURL *)URLForAssetsInDocumentsDirectory;

+ (NSString *)pathForJSBundleInDocumentsDirectory;

+ (NSString *)pathForAssetsInDocumentsDirectory;

+ (NSString *)pathForBundle;

@end

NS_ASSUME_NONNULL_END
